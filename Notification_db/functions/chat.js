const admin = require('firebase-admin');
exports.handler = ((change,context) => {
    const user_id = context.params.user_id;
    const from_user_id = context.params.from_user_id;
    const message_id = context.params.message_id;

    const fromUser = admin.database().ref(`/messages/${user_id}/${from_user_id}/${message_id}`).once('value');
    return fromUser.then(fromUserResult => {
      const from_user_id = fromUserResult.val().from;
      const message_body = fromUserResult.val().message;

        const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
        const deviceToken = admin.database().ref('/Users/'+ user_id +'/device_token').once('value');
        
        return Promise.all([userQuery, deviceToken]).then(result => {
            const userName = result[0].val();
            const token_id = result[1].val(); 
            const payload = {
              notification: {
                title : "New Message Incoming",
                body: `${userName}: ` + message_body,
                icon: "default",
                click_action : "com.example.finchat_TARGET_CHAT_NOTIFICATION"
              },
              data : {
                from_user_id : from_user_id,
                from_user_name : userName
              }
            };
        
            return admin.messaging().sendToDevice(token_id,payload).then(response => {
              return console.log('This was the notification feature');
            });
          });
        });
})