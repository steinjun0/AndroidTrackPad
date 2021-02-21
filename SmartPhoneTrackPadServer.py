import socket
import threading
import time
import win32api
import win32con
# binder함수는 서버에서 accept가 되면 생성되는 socket 인스턴스를 통해 client로 부터 데이터를 받으면 echo형태로 재송신하는 메소드이다.


class MouseController:
    def __init__(self):
        self.touchX = 0
        self.touchY = 0
        self.flag = 0
        self.touchX_t = 0
        self.touchY_t = 0

    def setTouch_t(self, touchX_t, touchY_t):
        self.touchX_t = touchX_t
        self.touchY_t = touchY_t

    def moveCursor(self, touchX, touchY, flag):
        self.touchX = touchX
        self.touchY = touchY

        print("touch pos("+str(touchX)+", " + str(touchY) + ")"+self.flag)
        curX, curY = win32api.GetCursorPos()
        print("delta("+str((touchY-self.touchY_t)) +
              ", " + str((touchX-self.touchX_t)) + ")")

        if(abs(self.touchY_t-touchY) > 1 or abs(touchX-self.touchX_t) > 1):
            win32api.SetCursorPos(
                (int(curX + (self.touchY_t-touchY)), int(curY + (touchX-self.touchX_t))))
            self.flag = '2'

        self.touchX_t = touchX
        self.touchY_t = touchY

    def wheelCursor(self, touchX, touchY, flag):
        self.touchX = touchX
        self.touchY = touchY

        print("touch pos("+str(touchX)+", " + str(touchY) + ")"+self.flag)
        curX, curY = win32api.GetCursorPos()
        print("delta("+str((touchY-self.touchY_t)) +
              ", " + str((touchX-self.touchX_t)) + ")")

        win32api.mouse_event(win32con.MOUSEEVENTF_WHEEL, int(
            curX + (self.touchY_t-touchY)), int(curY + (touchX-self.touchX_t)), 3*int((touchX-self.touchX_t)))

        self.touchX_t = touchX
        self.touchY_t = touchY

        self.flag = '3'


def binder(client_socket, addr):

    # 커넥션이 되면 접속 주소가 나온다.
    print('Connected by', addr)

    try:
        # 접속 상태에서는 클라이언트로 부터 받을 데이터를 무한 대기한다.
        # 만약 접속이 끊기게 된다면 except가 발생해서 접속이 끊기게 된다.
        touchX_t = 0
        touchY_t = 0
        curX = 0
        curY = 0
        mouseManager = MouseController()
        # mouseManager.setTouch_t(0,0)

        while True:
            # time.sleep(0.001)
            print("\n")
            # socket의 recv함수는 연결된 소켓으로부터 데이터를 받을 대기하는 함수입니다. 최초 4바이트를 대기합니다.
            data = client_socket.recv(25)
            # 최초 4바이트는 전송할 데이터의 크기이다. 그 크기는 little big 엔디언으로 byte에서 int형식으로 변환한다.
            # C#의 BitConverter는 big엔디언으로 처리된다.
            #length = int.from_bytes(data, "little")
            # 다시 데이터를 수신한다.
            #data = client_socket.recv(length)
            # 수신된 데이터를 str형식으로 decode한다.
            msg = data.decode().rstrip(' \t\r\n\0'+chr(21))
            # 수신된 메시지를 콘솔에 출력한다.
            print('Received from', addr, msg)
            try:
                print(mouseManager.flag)

                if msg[0] == 'X':
                    flag = msg[-1]
                    if ord(msg[-1]) == 0:
                        flag = msg[-4]

                    if flag == '0':
                        print("up!")
                        if(mouseManager.flag == '1'):
                            win32api.mouse_event(
                                win32con.MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0)
                            win32api.mouse_event(
                                win32con.MOUSEEVENTF_LEFTUP, 0, 0, 0, 0)
                        mouseManager.flag = '0'

                        pass
                    elif flag == '1':
                        print("click!")
                        mouseManager.setTouch_t(
                            int(int(msg[3:8])), int(int(msg[14:19])))
                        mouseManager.flag = '1'
                    elif flag == '3':
                        if mouseManager.flag == '0':
                            mouseManager.setTouch_t(
                                int(int(msg[3:8])), int(int(msg[14:19])))
                        print("wheel!")
                        touchX = int(int(msg[3:8]))
                        touchY = int(int(msg[14:19]))
                        mouseManager.wheelCursor(touchX, touchY, flag)

                    else:
                        if mouseManager.flag == '1' or mouseManager.flag == '2':
                            print("move!")
                            touchX = int(int(msg[3:8]))
                            touchY = int(int(msg[14:19]))
                            mouseManager.moveCursor(touchX, touchY, flag)

                        elif mouseManager.flag == '3':
                            if mouseManager.flag == '0':
                                mouseManager.setTouch_t(
                                    int(int(msg[3:8])), int(int(msg[14:19])))
                            touchX = int(int(msg[3:8]))
                            touchY = int(int(msg[14:19]))
                            mouseManager.wheelCursor(touchX, touchY, flag)

                        elif mouseManager.flag == '0':
                            mouseManager.setTouch_t(
                                int(int(msg[3:8])), int(int(msg[14:19])))
                            print("move!")
                            touchX = int(int(msg[3:8]))
                            touchY = int(int(msg[14:19]))
                            mouseManager.moveCursor(touchX, touchY, flag)

                elif msg[4] != 'X':
                    pass
                else:
                    flag = msg[-1]
                    if ord(msg[-1]) == 0:
                        flag = msg[-4]

                    if flag == '0':
                        print("up!")
                        if(mouseManager.flag == '1'):
                            win32api.mouse_event(
                                win32con.MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0)
                            win32api.mouse_event(
                                win32con.MOUSEEVENTF_LEFTUP, 0, 0, 0, 0)
                        mouseManager.flag = '0'

                        pass
                    elif flag == '1':
                        print("click!")
                        mouseManager.setTouch_t(
                            int(int(msg[7:12])), int(int(msg[18:23])))
                        mouseManager.flag = '1'
                    elif flag == '3':
                        if mouseManager.flag == '0':
                            mouseManager.setTouch_t(
                                int(int(msg[7:12])), int(int(msg[18:23])))
                        print("wheel!")
                        touchX = int(int(msg[7:12]))
                        touchY = int(int(msg[18:23]))
                        mouseManager.wheelCursor(touchX, touchY, flag)

                    else:
                        if mouseManager.flag == '1' or mouseManager.flag == '2':
                            print("move!")
                            touchX = int(int(msg[7:12]))
                            touchY = int(int(msg[18:23]))
                            mouseManager.moveCursor(touchX, touchY, flag)

                        elif mouseManager.flag == '3':
                            touchX = int(int(msg[7:12]))
                            touchY = int(int(msg[18:23]))
                            mouseManager.wheelCursor(touchX, touchY, flag)

                        elif mouseManager.flag == '0':
                            mouseManager.setTouch_t(
                                int(int(msg[7:12])), int(int(msg[18:23])))
                            print("move!")
                            touchX = int(int(msg[7:12]))
                            touchY = int(int(msg[18:23]))
                            mouseManager.moveCursor(touchX, touchY, flag)

                #print("("+msg[3:8]+", " + msg[14:19] + ")" if msg[0] == 'X' else "("+msg[7:12]+", " + msg[18:23] + ")")

            except:
                pass

            # 수신된 메시지 앞에 「echo:」 라는 메시지를 붙힌다.
            msg = "echo : " + msg
            # 바이너리(byte)형식으로 변환한다.
            data = msg.encode()
            # 바이너리의 데이터 사이즈를 구한다.
            length = len(data)
            # 데이터 사이즈를 little 엔디언 형식으로 byte로 변환한 다음 전송한다.
            client_socket.sendall(length.to_bytes(4, byteorder='little'))
            # 데이터를 클라이언트로 전송한다.
            client_socket.sendall(data)
    except:
        # 접속이 끊기면 except가 발생한다.
        print("except : ", addr)
    finally:
        # 접속이 끊기면 socket 리소스를 닫는다.
        client_socket.close()


# 소켓을 만든다.
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# 소켓 레벨과 데이터 형태를 설정한다.
server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
# 서버는 복수 ip를 사용하는 pc의 경우는 ip를 지정하고 그렇지 않으면 None이 아닌 ''로 설정한다.
# 포트는 pc내에서 비어있는 포트를 사용한다. cmd에서 netstat -an | find "LISTEN"으로 확인할 수 있다.
server_socket.bind(('', 3333))
# server 설정이 완료되면 listen를 시작한다.
server_socket.listen()

try:
    # 서버는 여러 클라이언트를 상대하기 때문에 무한 루프를 사용한다.
    while True:
        # client로 접속이 발생하면 accept가 발생한다.
        # 그럼 client 소켓과 addr(주소)를 튜플로 받는다.
        client_socket, addr = server_socket.accept()
        th = threading.Thread(target=binder, args=(client_socket, addr))
        # 쓰레드를 이용해서 client 접속 대기를 만들고 다시 accept로 넘어가서 다른 client를 대기한다.
        th.start()
except:
    print("server")
finally:
    # 에러가 발생하면 서버 소켓을 닫는다.
    server_socket.close()
