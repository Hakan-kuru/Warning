const {sendEmergencyService} = require('./emergencyService');
const {HttpsError} = require('firebase-functions/v2/https');

async function sendEmergencyHandler(request) {
  if (!request.auth) {
    throw new HttpsError('unauthenticated', 'Giriş yapmalısınız.');
  }

  const senderId = request.auth.uid;
  const {latitude, longitude} = request.data || {};

  if (!latitude || !longitude) {
    throw new HttpsError('invalid-argument', 'Konum zorunludur.');
  }

  return await sendEmergencyService(senderId, latitude, longitude);
}

module.exports = {sendEmergencyHandler};
