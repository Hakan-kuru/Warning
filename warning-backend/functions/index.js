const admin = require('firebase-admin');
admin.initializeApp();

const {onCall} = require('firebase-functions/v2/https');
const {sendEmergencyHandler} = require('./emergency/sendEmergency');

exports.sendEmergency = onCall(
    {
      region: 'europe-west1',
      timeoutSeconds: 30,
      memory: '256MiB',
    },
    sendEmergencyHandler,
);
