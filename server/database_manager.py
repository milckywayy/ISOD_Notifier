import sqlite3


class DatabaseManager:
    def __init__(self, db_name):
        self.db_name = db_name
        self.conn = None
        self.cursor = None

    def open(self):
        self.conn = sqlite3.connect(self.db_name)
        self.cursor = self.conn.cursor()

        self.execute('''CREATE TABLE IF NOT EXISTS clients (
                            token TEXT PRIMARY KEY,
                            username TEXT NOT NULL,
                            api_key TEXT NOT NULL,
                            version TEXT NOT NULL
                          )''')

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

            return self.cursor
        except sqlite3.IntegrityError:
            pass

    def fetchall(self):
        return self.cursor.fetchall()

    def fetchone(self):
        return self.cursor.fetchone()

    def commit(self):
        if self.conn:
            self.conn.commit()
