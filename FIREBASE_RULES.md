# Firebase rules


These should be added to the Firebase Realtime Database rules config.


```
{
  "rules": {

    // Users table
    "users": {
      ".indexOn": ["userId", "createdDate"],
      "$uid": {
        ".write": "auth != null && $uid === auth.uid",
        ".read": "auth != null && $uid === auth.uid"
      }
    },

    // Gratitude lists: only children with userId matching auth user
    "gratitude_lists": {
      ".indexOn": ["gratitudeListId", "createdDate"],
      ".read": "(auth !== null && query.orderByChild == 'userId' && query.equalTo == auth.uid)",
        "$gid": {
        ".write": "auth !== null && auth.uid === data.child('userId').val() || (!data.exists() && newData.child('userId').val() == auth.uid)",
        ".read": "auth !== null && auth.uid === data.child('userId').val() || (auth !== null && query.orderByChild == 'userId' && query.equalTo == auth.uid)"

      }
    },

    // Journal entries: only children with userId matching auth user
    "journal_entries": {
      ".indexOn": ["journalEntryId", "createdDate"],
      ".read": "(auth !== null && query.orderByChild == 'userId' && query.equalTo == auth.uid)",
      "$journalEntryId": {
        ".write": "auth !== null && auth.uid === data.child('userId').val() || (!data.exists() && newData.child('userId').val() == auth.uid)",
        ".read": "auth !== null && auth.uid === data.child('userId').val() || (auth !== null && query.orderByChild == 'userId' && query.equalTo == auth.uid)"
      }
    },


    // Logins: only children with userId matching auth user
    "logins": {
      ".indexOn": ["uid", "loginDate"],
      "$loginId": {
        ".write": "auth !== null && auth.uid === data.child('uid').val()",
        ".read": "auth !== null && auth.uid === data.child('uid').val()"
      }
    }

   }
}
```
