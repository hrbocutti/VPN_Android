package com.example.vpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Client extends VpnService{

    private static final String TAG = "VPN";
    private Thread mThread;
    private ParcelFileDescriptor mInterface;
    //a. Configure a builder for the interface.
    Builder builder = new Builder();
    private long IDLE_INTERVAL_MS = 100;
    private long KEEPALIVE_INTERVAL_MS = 100;
    private long RECEIVE_TIMEOUT_MS = 1000;

    // Services interface
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        String username = (String) intent.getExtras().get("username");
        String password = (String) intent.getExtras().get("password");

        mThread = new Thread(() -> {

            try {

                mInterface = builder.setSession("MyVPNService")
                        .addAddress("51.89.161.215", 24)
                        .addDnsServer("8.8.8.8")
                        .addRoute("0.0.0.0", 0)
                        .establish();

                FileInputStream in = new FileInputStream(
                        mInterface.getFileDescriptor());
                FileOutputStream out = new FileOutputStream(
                        mInterface.getFileDescriptor());
                DatagramChannel tunnel = DatagramChannel.open();

                tunnel.socket().bind(new InetSocketAddress(0));

                int port = tunnel.socket().getLocalPort();

                Log.d("VPNservice", "2 " + (tunnel != null));
                Log.d("VPNservice", "3 " + tunnel.connect(new InetSocketAddress(port)));

                tunnel.configureBlocking(false);
                protect(tunnel.socket());

                ByteBuffer packet = ByteBuffer.allocate(Short.MAX_VALUE);
                long lastReceiveTime = System.currentTimeMillis();
                long lastSendTime = System.currentTimeMillis();

                while (true) {

                    boolean idle = true;

                    int length = in.read(packet.array());

                    if (length > 0) {
                        packet.limit(length);
                        tunnel.write(packet);
                        packet.clear();
                        idle = false;
                        lastReceiveTime = System.currentTimeMillis();

                        Log.d("Log", "Write package");
                    }

                    length = tunnel.read(packet);
                    if (length > 0) {
                        if (packet.get(0) != 0){
                            out.write(packet.array(), 0, length);
                        }
                        packet.clear();
                        idle = false;
                        lastSendTime = System.currentTimeMillis();
                    }

                    if (idle) {
                        Thread.sleep(IDLE_INTERVAL_MS);
                        final long timeNow = System.currentTimeMillis();
                        if (lastSendTime + KEEPALIVE_INTERVAL_MS <= timeNow) {
                            packet.put((byte) 0).limit(1);
                            for (int i = 0; i < 3; ++i) {
                                packet.position(0);
                                tunnel.write(packet);
                            }
                            packet.clear();
                            lastSendTime = timeNow;
                        } else if (lastReceiveTime + RECEIVE_TIMEOUT_MS <= timeNow) {
                            throw new IllegalStateException("Timed out");
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (mInterface != null) {
                        mInterface.close();
                        mInterface = null;
                    }
                } catch (Exception e) {
                    Log.d("Erro: ", e.getMessage());
                }
            }
        }, "MyVpnRunnable");

        mThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (mThread != null) {
            mThread.interrupt();
        }
        super.onDestroy();
    }

}
