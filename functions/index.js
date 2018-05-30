'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


exports.sendNotification = functions.database.ref('/Notifications/{user_id}/{notification_id}').onWrite((change, context) => {

  const user_id = context.params.user_id;
  const notification_id = context.params.notification_id;

  console.log('User ID', context.params.user_id);

  /*if(!context.data.val()){
    return console.log('A notification has been deleted from the database : ', context.params.notification_id);
  }*/

  const fromUser = admin.database().ref(`/Notifications/${user_id}/${notification_id}`).once('value');
  return fromUser.then(fromUserResult => {

    const fromUserId = fromUserResult.val().from;
    console.log('You have a new notification from : ', fromUserId);

    const userQuery = admin.database().ref(`Users/${fromUserId}/username`).once('value');
    return userQuery.then(userResult => {

    const uName = userResult.val();

    const dToken = admin.database().ref(`/Users/${user_id}/deviceToken`).once('value');

    return dToken.then(result => {

      const tokenId = result.val();

      const payload = {
        notification: {
          title: "Pairing Request",
          body: `${uName} has sent you a Pair Request`,
          icon: "default",
          click_action: "com.example.markenteder.locktalk.TARGET_NOTIFICATION"
        },
        data: {
          fromUserId : fromUserId
        }
      };

      return admin.messaging().sendToDevice(tokenId, payload).then(response => {
        console.log('This was the notification feature')
      });
    });
    });
  });
  return true;
});
