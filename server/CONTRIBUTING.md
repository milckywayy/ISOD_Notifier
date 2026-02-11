# Contributing to ISOD-USOS Notifier

This project is a middleware service connecting Firebase, USOS API, and ISOD. Because it handles sensitive university data, the repository does not include configuration files or keys. 

Follow these steps to set up your development environment.

## Setup Instructions

### Environment Preparation

1.  Clone the repo
    ```sh
    git clone https://github.com/milckywayy/ISOD_Notifier
    cd ISOD_Notifier
    ```

2. Create and activate virtual environment
    ```sh
    python -m venv venv
    # Windows: venv\Scripts\activate
    # Mac/Linux: source venv/bin/activate

    # Install dependencies
    pip install -r requirements.txt
    ```

### Credentials Configuration
The application requires a `credentials/` directory. You must place the following files inside credentials directory:

1. Firebase Service Account
    - File: `isod-notifier-6c6a8e2eca56.json`
    - Source: Firebase Console > Project Settings > Service Accounts > Generate New Private Key.

2. USOS API Credentials
    - File: `usos_api_credentials.json`
    - Source: https://apps.usos.pw.edu.pl/developers/
    - Content Example:
        ```
        {
            "api_base_address": "https://apps.usos.pw.edu.pl/",
            "consumer_key": "YOUR_KEY",
            "consumer_secret": "YOUR_SECRET"
        }
        ```