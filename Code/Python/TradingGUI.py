#https://likegeeks.com/python-gui-examples-tkinter-tutorial/
#https://realpython.com/python-gui-tkinter/
#https://realpython.com/api-integration-in-python/
#https://www.geeksforgeeks.org/create-table-using-tkinter/
#https://pythonguides.com/python-tkinter-table-tutorial/
#https://github.com/Apfirebolt/StockMax-Stock-Trading-Application-in-python-using-tkinter
#https://github.com/CoryHunter/TradingApp
#https://www.geeksforgeeks.org/python-gui-tkinter/

from tkinter import *
from tkinter import ttk
import requests
import json
import threading
import time

datarequest_url = "http://localhost:9080/USERID=1;INSTR=DATA;SEC=BOO"
send_order_request_url = "http://localhost:9080/USERID={};INSTR={};SEC={};PRICE={};SIZE={}"
delete_order_request_url = "http://localhost:9080/USERID={};INSTR=DELETEORDER;ORDERID={}"
check_order_request_url = "http://localhost:9080/USERID={};INSTR=CHECKORDER;ORDERID={}"

#working_orders[100] = ["BOO", "BUY", 2, 10.01]
#working_orders[101] = ["BOO", "SELL", 3, 10.02]
orderids = []

window = Tk()
window.title("ExSim Trading GUI")
window.geometry('800x600')

def connect():
    aThread = threading.Thread(target=thread_task)
    aThread.start()

def send_order():
    userid = userid_entry.get()
    sec = sec_entry.get()
    size = size_entry.get()
    action = action_combo.get()
    price = price_entry.get()
    request_url = send_order_request_url.format(userid, action, sec, price, size)
    print("send_order request url " + request_url)
    response = requests.get(request_url)
    print("send_order response text " + response.text)
    orderid = int(response.text)
    orderids.append(orderid)
    #working_orders[orderid] = [sec, action, size, price]

def delete_order():
    userid = userid_entry.get()
    orderid = orderid_entry.get()
    request_url = delete_order_request_url.format(userid, orderid)
    print("delete_order request url " + request_url)
    response = requests.get(request_url)
    print("delete_order response text " + response.text)
    
def draw_inputs():
    lab = Label(window, width = 10, bg = 'white', text = "USERID").place(relx= 0.05, rely = 0.10)
    global userid_entry
    userid_entry = Entry(window, width = 20, bg = 'white')
    userid_entry.place(relx= 0.20, rely = 0.10)
    userid_entry.insert(0, "100")

    lab = Label(window, width = 10, bg = 'white', text = "SEC").place(relx= 0.05, rely = 0.15)
    global sec_entry
    sec_entry = Entry(window, width = 20, bg = 'white')
    sec_entry.place(relx= 0.20, rely = 0.15)
    sec_entry.insert(0, "BOO")

    lab = Label(window, width = 10, bg = 'white', text = "SIZE").place(relx= 0.05, rely = 0.20)
    global size_entry
    size_entry = Entry(window, width = 20, bg = 'white')
    size_entry.place(relx= 0.20, rely = 0.20)
    size_entry.insert(0, "1")
    
    lab = Label(window, width = 10, bg = 'white', text = "ACTION").place(relx= 0.05, rely = 0.25)
    global action_combo
    action_combo = ttk.Combobox(window, values=["BUY", "SELL"])
    action_combo.place(relx = 0.20, rely = 0.25)
    action_combo.current(0)
    
    lab = Label(window, width = 10, bg = 'white', text = "PRICE").place(relx= 0.05, rely = 0.30)
    global price_entry
    price_entry = Entry(window, width = 20, bg = 'white')
    price_entry.place(relx= 0.20, rely = 0.30)
    price_entry.insert(0, "9.99")
    
    btn = Button(window, text="Send Order", command=send_order)
    btn.place(relx=0.10, rely = 0.35)
    
    lab = Label(window, width = 10, bg = 'white', text = "ORDERID").place(relx= 0.40, rely = 0.10)
    global orderid_entry
    orderid_entry = Entry(window, width = 20, bg = 'white')
    orderid_entry.place(relx= 0.55, rely = 0.10)
    orderid_entry.insert(0, "10")
    
    btn = Button(window, text="Delete Order", command=delete_order)
    btn.place(relx=0.45, rely = 0.15)


def update_orders():
        
    lab = Label(window, text = "Orders", font=("Arial", 16))
    lab.place(relx = 0.2, rely = 0.70, anchor = S)
    
    labels = " Status"
    labels = pad_string(labels, 20)
    labels = labels + "Sec"
    labels = pad_string(labels, 40)
    labels = labels + "Action"
    labels = pad_string(labels, 60)
    labels = labels + "OrderID"
    labels = pad_string(labels, 80)
    labels = labels + "Price"
    labels = pad_string(labels, 100)
    labels = labels + "SizeDone"
    labels = pad_string(labels, 120)
    labels = labels + "SizeRemain"
    
    lab = Label(window, text = labels, bg = '#303030' , fg = "white")
    lab.place(relx = 0.5, rely = 0.65, anchor = S)
    
    box = Listbox(window, bg = "white", fg = "#303030", borderwidth = 5)
    box.place(relx = 0.5, rely = 0.95 , anchor = S, relwidth = 0.8, relheight = 0.30)
    
    #TODO add scrollbar
    
    userid = userid_entry.get()
    
    orders = {}
    for orderid in orderids:
        request_url = check_order_request_url.format(userid, orderid)
        response = requests.get(request_url)
        orders[orderid] = response.json()

    i = 0
    for orderid in orders:
        order = orders[orderid]
        line = ""
        line = pad_string(line, 18)
        line = line + order["Status"]
        line = pad_string(line, 37)
        line = line + order["Sec"]
        line = pad_string(line, 56)
        line = line + order["Action"]
        line = pad_string(line, 77)
        line = line + str(order["OrderID"])
        line = pad_string(line, 100)
        line = line + str(order["Price"])
        line = pad_string(line, 125)
        line = line + str(order["SizeDone"])
        line = pad_string(line, 151)
        line = line + str(order["SizeRemain"])
        
        box.insert(i, line)
        box.itemconfig(i, fg = "black")
        i=i+1
    
def pad_string(str, length):
    new_str = str
    while len(new_str) < length:
        new_str = new_str + " "
    return new_str
    

def thread_task():
    while True:
        update_ladder()
        update_orders()
        time.sleep(1)
        
def update_ladder():

    response = requests.get(datarequest_url)
    ladder_dict = response.json()

    lab = Label(window, text = "Market Depth", font=("Arial", 16))
    lab.place(relx = 0.5, rely = 0.35, anchor = S)
    
    labels = " BidSize"
    while (len(labels) < 20):
        labels = labels + " "
    labels = labels + "BidPrice"
    while (len(labels) < 40):
        labels = labels + " "
    labels = labels + "OfferPrice"
    while (len(labels) < 60):
        labels = labels + " " 
    labels = labels + "OfferSize"
    lab = Label(window, text = labels, bg = '#303030' , fg = "white")
    lab.place(relx = 0.5, rely = 0.40, anchor = S)
    
    box = Listbox(window, bg = "white", fg = "#303030", borderwidth = 5)
    box.place(relx = 0.5, rely = 0.60 , anchor = S, relwidth = 0.35, relheight = 0.20)
    
    line = ""
    line = pad_string(line, 50)
    line = line + str(ladder_dict["O3P"])
    line = pad_string(line, 75)
    line = line + str(ladder_dict["O3S"])
    box.insert(0, line)
    box.itemconfig(0, fg = "red")
    
    line = ""
    line = pad_string(line, 50)
    line = line + str(ladder_dict["O2P"])
    line = pad_string(line, 75)
    line = line + str(ladder_dict["O2S"])
    box.insert(1, line)
    box.itemconfig(1, fg = "red")
    
    line = ""
    line = pad_string(line, 50)
    line = line + str(ladder_dict["O1P"])
    line = pad_string(line, 75)
    line = line + str(ladder_dict["O1S"])
    box.insert(2, line)
    box.itemconfig(2, fg = "red")
    
    line = ""
    line = pad_string(line, 5)
    line = line + str(ladder_dict["B1S"])
    line = pad_string(line, 25)
    line = line + str(ladder_dict["B1P"])
    box.insert(3, line)
    box.itemconfig(3, fg = "green")
    
    line = ""
    line = pad_string(line, 5)
    line = line + str(ladder_dict["B2S"])
    line = pad_string(line, 25)
    line = line + str(ladder_dict["B2P"])
    box.insert(4, line)
    box.itemconfig(4, fg = "green")

    line = ""
    line = pad_string(line, 5)
    line = line + str(ladder_dict["B3S"])
    line = pad_string(line, 25)
    line = line + str(ladder_dict["B3P"])
    box.insert(5, line)
    box.itemconfig(5, fg = "green")

btn = Button(window, text="Connect", command=connect)
btn.place(relx = 0.02, rely = 0.02)
    
draw_inputs()

update_orders()

window.mainloop()