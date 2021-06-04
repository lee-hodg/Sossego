# Sossego

![Made with love in Brazil](https://madewithlove.now.sh/br?heart=true&template=for-the-badge)

## Screenshots


Sossego is a free, open-source Android app writen in Kotlin

Sossego features gratitude lists, journaling, configurable reminders and an adjustable meditation timer.
The hope is that Sossego will be useful for people wishing to generate more peace and mindfulness in their lives and help addicts
in recovery completing programs that involve such practices.


## Firebase rules

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
