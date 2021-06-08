from flask import Flask
from flask import render_template, request
from flask import *
import json
import qrcode
import uuid
import sqlite3
import datetime

now = datetime.datetime.now()

#cursor.execute("""CREATE TABLE IF NOT EXISTS data (id INTEGER PRIMARY KEY,
#    uuid TEXT,
#    date TEXT,
#    lat REAL,
 #   lng REAL)
#""")
#cursor.commi

app = Flask(__name__)
currentUUID = ""
@app.route('/')
def qr():
    currentUUID = uuid.uuid4()
    print(currentUUID)
    img = qrcode.make(currentUUID)
    img.save("static/qrimg.png")
    url_for('static', filename="app.js")
    url_for('static', filename="style.css")
    #with open("test.json", "r") as read_file:
    #    data = json.load(read_file)
    #print(data)
    return render_template("qr.html")

@app.route('/setpoint/', methods=["POST"])
def setPoint():
    json1 = request.get_json()
    print(json1)
    uuid = json1["uuid"]
    lat = json1["lat"]
    lng = json1["lng"]
    date = now.strftime("%d-%m-%Y %H:%M")
    writeDB(uuid, date, lat, lng)
    print(uuid,date, lat, lng)
    return "OK"

@app.route('/reg/', methods=["POST"])
def reg():
    json1 = request.get_json()
    uuid = json1["uuid"]
    lat = json1["lat"]
    lng = json1["lng"]
    if (uuid is None):
        uuid = request.form.get('uuid')
    writeDB(uuid, now.strftime("%d-%m-%Y %H:%M"), lat, lng)
    return "OK"


@app.route('/enter/', methods=["POST"])
def enter():
    uuid = request.form.get("uuid_field")
    print(uuid)
    db = sqlite3.connect("Database.db")
    cursor = db.cursor()
    row = None
    markerArr = []
    for row in cursor.execute("SELECT date, lat, lng FROM data WHERE uuid = ?", (uuid,)):
        markerArr.append(row)
        print(row)
    if (row is None):
        print("ГОВНО")
        return "NOT OK"
    else:
        return render_template("index.html", markers = markerArr)

def writeDB(uuid, date, lat, lng):
    db = sqlite3.connect("Database.db")
    cursor = db.cursor()
    cursor.execute("INSERT INTO data(uuid, date, lat, lng) VALUES(?,?,?,?)", (uuid, date, lat, lng))
    db.commit()

if __name__ == '__main__':
    app.run(host="192.168.43.252", port=25565)


#29b53ff5-5253-42f2-8b35-93ba500c8f03