# Sossego


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
      "$gid:": {
        ".write": "auth !== null && auth.uid === data.child('userId').val()",
        ".read": "auth !== null && auth.uid === data.child('userId').val()"
      }
    },

    // Journal entries: only children with userId matching auth user
    "journal_entries": {
      ".indexOn": ["journalEntryId", "createdDate"],
      "$journalEntryId": {
        ".write": "auth !== null && auth.uid === data.child('userId').val()",
        ".read": "auth !== null && auth.uid === data.child('userId').val()"
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