
# ISOD Notifier V1 API Documentation

This application serves as a notification management system, enabling users to register their devices and receive ISOD news updates.

Project repository on GitHub: [ISOD Notifier V1](https://github.com/milckywayy/ISOD_Notifier/tree/v1)

## Overview

### **Registration Data Management**
   - When a user registers via the API, the application stores their device token, username, API key, app version, and preferred language and notification filter in the database.
   - It also verifies the device token to ensure notifications can be sent reliably.
   - Any duplicate or outdated device information is identified and updated, allowing for consistent notification delivery.

### **User-Specified Notification Filters**
   - Filter parameter, received during registration, enables users to control the categories of notifications they receive (e.g., class updates, announcements).
   - This preference data is saved for each device, and during notification processing, it determines which updates each user is eligible to receive based on their selected filter categories.

### **Device and User Lifecycle Management**
   - The program regularly checks the database for inactive devices (based on invalid tokens) or users without any registered devices and removes this data to optimize storage and ensure current user status.
   - When a user unregisters via the API, their associated devices are de-registered. If a user has no remaining devices, their profile and related data are completely removed from the database.

### **Notification Processing and Delivery**
   - Notification data is periodically fetched from an external source and compared against the stored data to identify new items.
   - Only new notifications relevant to each user’s preferences are processed and sent, ensuring users receive timely and customized notifications without redundant updates.

### **Language and Localization**
   - User-selected language preferences, provided at registration, are stored and referenced to deliver notifications in the preferred language.

## API Endpoints

### 1. `POST /register`

#### Description
Registers a device and associates it with a user account. If the user and device are new, they are added to the database. The endpoint also sends a silent notification to verify the Firebase Cloud Messaging (FCM) token.

#### Request
- **URL**: `/register`
- **Method**: `POST`
- **Content-Type**: `application/json`

##### Request Body Parameters:
| Parameter     | Type   | Description                                |
|---------------|--------|--------------------------------------------|
| `token`       | String | Device FCM token.                         |
| `username`    | String | ISOD client username.                     |
| `api_key`     | String | ISOD API key associated with the user.    |
| `version`     | String | Version of the app on the device.         |
| `language`    | String | Language preference for notifications.    |
| `filter`      | Integer | Filter setting for news notifications.    |

###### Language Parameter (`language`)

The `language` parameter specifies the language in which the user wants to receive notifications. The system currently supports two languages:

- `pl` - Polish
- `en` - English

###### Filter Parameter (`filter`)

The `filter` value determines which types of notifications a user wants to receive. This filter is stored as a bitmask, allowing multiple notification categories to be enabled or disabled by setting specific bits. Each bit in the filter value corresponds to a different type of notification:

| Bit Position | Filter Category      | Description                                                     |
|--------------|----------------------|-----------------------------------------------------------------|
| 0            | Classes              | Notifications about class-related updates or changes.           |
| 1            | Announcements        | General announcements and updates from dean's office.          |
| 2            | WRS                  | Notifications related to the WRS.  |
| 3            | Other                | Miscellaneous notifications not categorized under other filters.|

Each bit in the `filter` is either:
- `1` - To enable notifications for that category.
- `0` - To disable notifications for that category.

For example, a `filter` value of `5` (binary `0101`) enables notifications for **Classes** and **WRS**, while disabling notifications for **Announcements** and **Other**.

##### Sample Request
```json
{
  "token": "example_token",
  "username": "example_username",
  "api_key": "example_api_key",
  "version": "1.0.1",
  "language": "en",
  "filter": 5
}
```

#### Responses
- **200 OK**: Registration successful.
- **400 Bad Request**: Invalid FCM token, username, or API key, or if the input data is improperly formatted.
- **500 Internal Server Error**: Database error or error communicating with external services.

##### Example Response
```json
{
  "message": "Device registered successfully."
}
```

---

### 2. `POST /unregister`

#### Description
Unregisters a device using its token. If there are no other registered devices associated with the user, the user and their related data are also removed from the database.

#### Request
- **URL**: `/unregister`
- **Method**: `POST`
- **Content-Type**: `application/json`

##### Request Body Parameters:
| Parameter   | Type   | Description                     |
|-------------|--------|---------------------------------|
| `token`     | String | Device FCM token.               |
| `username`  | String | ISOD client username.           |

##### Sample Request
```json
{
  "token": "example_token",
  "username": "example_username"
}
```

#### Responses
- **200 OK**: Unregistration successful.
- **400 Bad Request**: Invalid input data.
- **500 Internal Server Error**: Database error.

##### Example Response
```json
{
  "message": "Device unregistered successfully."
}
```

---

### 3. `POST /registration_status`

#### Description
Checks the registration status of a device using its token. If the device is registered, its app version is updated if different from the stored version.

#### Request
- **URL**: `/registration_status`
- **Method**: `POST`
- **Content-Type**: `application/json`

##### Request Body Parameters:
| Parameter     | Type   | Description                                 |
|---------------|--------|---------------------------------------------|
| `token`       | String | Device FCM token.                           |
| `version`     | String | Version of the app on the device.           |

##### Sample Request
```json
{
  "token": "example_token",
  "version": "1.0.1"
}
```

#### Responses
- **250 OK**: Device is registered.
- **251 OK**: Device is unregistered.
- **400 Bad Request**: Invalid input data.
- **500 Internal Server Error**: Database error.

##### Example Response
```json
{
  "message": "Device is registered."
}
```

---

#### Error Handling
- **400 Bad Request**: Occurs when input data is invalid, such as missing required fields or incorrectly formatted values.
- **500 Internal Server Error**: Occurs for internal database or server errors during processing.
