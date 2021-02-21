package com.example.smartphonetrackpad;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MainActivity extends AppCompatActivity {

    TextView textPointer;
    TextView textClick;
    TextView textTop;
    EditText textIP;
    Button buttonIP;
    String IP = "0.0.0.0";
    Socket socket = new Socket();
    SocketAddress addr;
    int flag = 0;

    float touchX = 0;
    float touchY = 0;

    float touchX_t = 0;
    float touchY_t = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;

        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        textPointer = (TextView) findViewById(R.id.textPointer);
        textTop = (TextView) findViewById(R.id.textTop);
        textClick = (TextView) findViewById(R.id.textClick);
        textIP = (EditText) findViewById(R.id.textIP);
        buttonIP = (Button) findViewById(R.id.buttonIP);
        buttonIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IP = textIP.getText().toString();
                try {
                    socket.connect(new InetSocketAddress(IP, 3333));

                    ConnectThread thread = new ConnectThread(IP);
                    thread.start();
                    textIP.setEnabled(false);
                    buttonIP.setEnabled(false);
                } catch (UnknownHostException uhe) {
// 소켓 생성 시 전달되는 호스트(www.unknown-host.com)의 IP를 식별할 수 없음.

                    Log.e("socekt", " 생성 Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트이름 사용)");
                    Toast.makeText(getApplicationContext(), "IP 주소를 식별할 수 없음", Toast.LENGTH_SHORT).show();
                } catch (IOException ioe) {
// 소켓 생성 과정에서 I/O 에러 발생. 주로 네트워크 응답 없음.

                    Log.e("socekt", " 생성 Error : 네트워크 응답 없음");
                    Toast.makeText(getApplicationContext(), "네트워크 응답 없음", Toast.LENGTH_SHORT).show();
                } catch (SecurityException se) {
// security manager에서 허용되지 않은 기능 수행.

                    Log.e("socekt", " 생성 Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)");
                    Toast.makeText(getApplicationContext(), "보안(Security) 위반", Toast.LENGTH_SHORT).show();
                } catch (IllegalArgumentException le) {
// 소켓 생성 시 전달되는 포트 번호(65536)이 허용 범위(0~65535)를 벗어남.

                    Log.e("socekt", " 생성 Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)");
                    Toast.makeText(getApplicationContext(), "0~65535 범위 밖의 포트", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();
        touchX = event.getX();
        touchY = event.getY();

        textPointer.setText("(" + Float.toString(touchX) + ", " + Float.toString(touchY) + ")");

        switch (action) {
            case MotionEvent.ACTION_UP:
                flag = 0;
                textClick.setText("Up");
                break;

            case MotionEvent.ACTION_DOWN:
                flag = 1;
                textClick.setText("Down");
                break;

            case MotionEvent.ACTION_MOVE:
                flag = 2;
                textClick.setText("Move");
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                flag = 3;
                textClick.setText("Wheel");
                break;
        }

/*
        if (action == MotionEvent.ACTION_UP) {
            flag = 0;
            textClick.setText("Up");
        } else if (action == MotionEvent.ACTION_DOWN) {
            flag = 1;
            textClick.setText("Down");
        } else if (action == MotionEvent.ACTION_MOVE) {
            flag = 2;
            textClick.setText("Move");
        } else if(action == MotionEvent.ACTION_POINTER_DOWN){
            flag = 3;
            textClick.setText("Wheel");
        }*/
        return super.onTouchEvent(event);
    }

    class ConnectThread extends Thread {
        String hostname;


        public ConnectThread(String addr) {
            hostname = addr;
        }

        public void run() {
            try { //클라이언트 소켓 생성

                int port = 3333;
                socket = new Socket(hostname, port);
                Log.d("socekt", "Socket 생성, 연결.");


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textTop.setText("연결 완료");
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                    }
                });
                touchX_t = touchX;
                touchY_t = touchY;
                try (OutputStream sender = socket.getOutputStream(); InputStream receiver = socket.getInputStream();) {

// 메시지는 for 문을 통해 10번 메시지를 전송한다.

                    for (int i = 0; i < 0; i++) {
// 전송할 메시지를 작성한다.
                        String msg = "java test message - " + i;
// string을 byte배열 형식으로 변환한다.
                        byte[] data = msg.getBytes();
// ByteBuffer를 통해 데이터 길이를 byte형식으로 변환한다.
                        ByteBuffer b = ByteBuffer.allocate(4);
// byte포멧은 little 엔디언이다.
                        b.order(ByteOrder.LITTLE_ENDIAN);
                        b.putInt(data.length);
// 데이터 길이 전송
                        sender.write(b.array(), 0, 4);
// 데이터 전송
                        sender.write(data);
                        data = new byte[4];
                        /*
// 데이터 길이를 받는다.
                        receiver.read(data, 0, 4);
// ByteBuffer를 통해 little 엔디언 형식으로 데이터 길이를 구한다.
                        ByteBuffer c = ByteBuffer.wrap(data);
                        c.order(ByteOrder.LITTLE_ENDIAN);
                        int length = c.getInt();
// 데이터를 받을 버퍼를 선언한다.
                        data = new byte[length];
// 데이터를 받는다.
                        receiver.read(data, 0, length);
// byte형식의 데이터를 string형식으로 변환한다.
                        msg = new String(data, "UTF-8");
// 콘솔에 출력한다.
                        System.out.println(msg);
                        */
                    }
                    int k = 0;
                    int w = 1;
                    while(true){
                        Thread.sleep(10);
                        if(touchX != touchX_t || touchY != touchY_t){
                            k=0;
                            if(flag == 0)
                                w = 3;
                            while(k<w) {
                                Log.d("touch", Integer.toString(flag)+Integer.toString(w));
                                String msg = String.format("X: %06d, Y: %06d%1d", (int) (touchX * 10), (int) (touchY * 10), flag);
// string을 byte배열 형식으로 변환한다.
                                byte[] data = msg.getBytes();
// ByteBuffer를 통해 데이터 길이를 byte형식으로 변환한다.
                                ByteBuffer b = ByteBuffer.allocate(4);
// byte포멧은 little 엔디언이다.
                                b.order(ByteOrder.LITTLE_ENDIAN);
                                b.putInt(data.length);
// 데이터 길이 전송
                                sender.write(b.array(), 0, 4);
// 데이터 전송
                                sender.write(data);
                                data = new byte[4];

                                touchX_t = touchX;
                                touchY_t = touchY;

                                if(flag == 2){
                                    k=0;
                                    break;
                                }
                                else{
                                    k++;
                                    Thread.sleep(1);
                                    continue;
                                }
                            }
                        }
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            } catch (IOException ioe) {
                // 소켓 생성 과정에서 I/O 에러 발생.

                Log.e("socekt", " 생성 Error : 네트워크 응답 없음");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 네트워크 응답 없음",
                                Toast.LENGTH_SHORT).show();
                        textTop.setText("네트워크 연결 오류");
                    }
                });


            } catch (SecurityException se) {
                // security manager에서 허용되지 않은 기능 수행.

                Log.e("socekt", " 생성 Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생.(프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)", Toast.LENGTH_SHORT).show();
                        textTop.setText("Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)");
                    }
                });


            } catch (IllegalArgumentException le) {
                // 소켓 생성 시 전달되는 포트 번호(65536)이 허용 범위(0~65535)를 벗어남.

                Log.e("socekt", " 생성 Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생. (0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), " Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생. (0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)", Toast.LENGTH_SHORT).show();
                        textTop.setText("Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생. (0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)");
                    }
                });

            }

        }
    }
}