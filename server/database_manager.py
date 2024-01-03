import sqlite3


class DatabaseManager:
    def __init__(self, db_name):
        self.db_name = db_name
        self.conn = None
        self.cursor = None

    def open(self):
        self.conn = sqlite3.connect(self.db_name)
        self.cursor = self.conn.cursor()

        self.execute('''
            CREATE TABLE IF NOT EXISTS clients (
                username TEXT PRIMARY KEY NOT NULL,
                api_key TEXT NOT NULL
            );
        ''')

        self.execute('''
            CREATE TABLE IF NOT EXISTS devices (
                token TEXT PRIMARY KEY NOT NULL,
                version TEXT NOT NULL,
                username TEXT NOT NULL,
                filter INTEGER NOT NULL,
                FOREIGN KEY (username) REFERENCES clients(username)
            );
        ''')

        self.execute('''
            CREATE TABLE IF NOT EXISTS news (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                hash TEXT NOT NULL,
                type TEXT NOT NULL,
                date TEXT NOT NULL,
                FOREIGN KEY (username) REFERENCES clients(username)
            );
        ''')

    def close(self):
        if self.conn:
            self.conn.commit()
            self.cursor.close()
            self.conn.close()

    def execute(self, query, params=()):
        try:
            if not self.conn:
                self.open()
            self.cursor.execute(query, params)

            return self.cursor.fetchall()

        except sqlite3.IntegrityError:
            pass

    def commit(self):
        if self.conn:
            self.conn.commit()
