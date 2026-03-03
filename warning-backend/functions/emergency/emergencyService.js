const admin = require('firebase-admin');
const {FieldValue, GeoPoint} = require('firebase-admin/firestore');

const db = admin.firestore();
const messaging = admin.messaging();

const RATE_LIMIT_SECONDS = 30;

async function sendEmergencyService(senderId, latitude, longitude) {
  // ------------------------------------------------
  // 1️⃣ RATE LIMIT KONTROL
  // ------------------------------------------------
  const metaRef = db.collection('users_meta').doc(senderId);
  const metaDoc = await metaRef.get();

  const now = Date.now();

  if (metaDoc.exists) {
    const lastEmergency = metaDoc.data().lastEmergencyAt ?
      metaDoc.data().lastEmergencyAt.toMillis() :
      null; if (lastEmergency && (now - lastEmergency) < RATE_LIMIT_SECONDS * 1000) {
      throw new Error('Çok sık acil durum gönderemezsiniz.');
    }
  }

  await metaRef.set({
    lastEmergencyAt: FieldValue.serverTimestamp(),
  }, {merge: true});


  // ------------------------------------------------
  // 2️⃣ SENDER PROFİLİ
  // ------------------------------------------------
  const senderDoc = await db.collection('profiles').doc(senderId).get();
  if (!senderDoc.exists) {
    throw new Error('Gönderen bulunamadı.');
  }

  const senderData = senderDoc.data();
  const defaultEmergencyMsg = senderData.emergencyMessage;
  const senderName = senderData.name || 'Birisi';


  // ------------------------------------------------
  // 3️⃣ CONTACT SORGUSU
  // ------------------------------------------------
  const contactsQuery = await db.collection('contacts')
      .where('addingId', '==', senderId)
      .where('isConfirmed', '==', true)
      .where('isTop', '==', true)
      .where('isActiveUser', '==', true)
      .get();

  if (contactsQuery.empty) {
    return {successCount: 0, failureCount: 0};
  }

  const messages = [];
  const historyLogs = [];
  const invalidTokens = [];

  for (const contactDoc of contactsQuery.docs) {
    const contactData = contactDoc.data();
    const receiverId = contactData.addedId;
    if (!receiverId) continue;

    const receiverDoc = await db.collection('profiles').doc(receiverId).get();
    if (!receiverDoc.exists) continue;

    const receiverData = receiverDoc.data();
    const token = receiverData.fcmToken;
    if (!token) continue;

    // Mesaj fallback
    let finalMessage = 'yardım edin';
    if (contactData.specialMessage) {
      finalMessage = contactData.specialMessage;
    } else if (defaultEmergencyMsg) {
      finalMessage = defaultEmergencyMsg;
    }

    const includeLocation = contactData.isLocationSend === true;

    messages.push({
      token,
      notification: {
        title: 'Acil Durum Mesajı',
        body: finalMessage,
      },
      data: {
        type: 'EMERGENCY',
        senderId,
        senderName,
        latitude: includeLocation ? String(latitude) : '',
        longitude: includeLocation ? String(longitude) : '',
        hasLocation: includeLocation ? 'true' : 'false',
        sentTime: String(Date.now()),
      },
      android: {
        priority: 'high',
        notification: {
          channelId: 'emergency_channel',
          defaultSound: true,
        },
      },
    });

    historyLogs.push({
      senderId,
      receiverId,
      senderName,
      messageContent: finalMessage,
      hasLocation: includeLocation,
      location: includeLocation ? new GeoPoint(latitude, longitude) : null,
      timestamp: FieldValue.serverTimestamp(),
      status: 'pending',
      error: null,
    });
  }

  if (messages.length === 0) {
    return {successCount: 0, failureCount: 0};
  }


  // ------------------------------------------------
  // 4️⃣ SEND EACH
  // ------------------------------------------------
  const response = await messaging.sendEach(messages);

  const batch = db.batch();
  const historyCollection = db.collection('emergency_history');

  response.responses.forEach((res, index) => {
    if (!res.success) {
      historyLogs[index].status = 'failed';
      historyLogs[index].error = JSON.stringify(res.error);

      if (
        res.error.code === 'messaging/registration-token-not-registered'
      ) {
        invalidTokens.push(messages[index].token);
      }
    } else {
      historyLogs[index].status = 'sent';
    }

    const docRef = historyCollection.doc();
    batch.set(docRef, historyLogs[index]);
  });

  await batch.commit();


  // ------------------------------------------------
  // 5️⃣ INVALID TOKEN TEMİZLEME
  // ------------------------------------------------
  if (invalidTokens.length > 0) {
    const profilesQuery = await db.collection('profiles')
        .where('fcmToken', 'in', invalidTokens.slice(0, 10))
        .get();

    const cleanBatch = db.batch();
    profilesQuery.docs.forEach((doc) => {
      cleanBatch.update(doc.ref, {fcmToken: FieldValue.delete()});
    });

    await cleanBatch.commit();
  }

  return {
    successCount: response.successCount,
    failureCount: response.failureCount,
  };
}

module.exports = {sendEmergencyService};
