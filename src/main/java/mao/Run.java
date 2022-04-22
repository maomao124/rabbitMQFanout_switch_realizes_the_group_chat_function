package mao;

import com.rabbitmq.client.*;
import mao.tools.RabbitMQ;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Project name(项目名称)：rabbitMQFanout交换机实现群聊功能
 * Package(包名): mao
 * Class(类名): Run
 * Author(作者）: mao
 * Author QQ：1296193245
 * GitHub：https://github.com/maomao124/
 * Date(创建日期)： 2022/4/22
 * Time(创建时间)： 20:17
 * Version(版本): 1.0
 * Description(描述)： 无
 */

public class Run
{
    //交换机名称
    private static final String EXCHANGE_NAME = "fanout_exchange";
    //队列名称
    private static String queueName;
    //发送信道
    private static Channel channel_send;
    //接收信道
    private static Channel channel_receive;
    //顶层面板
    JFrame jFrame;


    static
    {
        try
        {
            channel_send = RabbitMQ.getChannel();
            channel_receive = RabbitMQ.getChannel();

            //关于接收
            //获得队列名字
            queueName = channel_receive.queueDeclare().getQueue();
            //声明交换机
            channel_receive.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            //绑定
            channel_receive.queueBind(queueName, EXCHANGE_NAME, "chat");
        }
        catch (Exception e)
        {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "发生异常！异常内容：\n" + e.getMessage());
            System.out.println("退出");
            System.exit(1);
        }
    }

    public Run()
    {
        //初始化顶层面板
        jFrame = new JFrame("群聊软件  " + queueName);
        jFrame.setSize(1280, 720);
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;       //获取屏幕宽度
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;     //获取屏幕高度
        jFrame.setLocation(screenWidth / 2 - jFrame.getWidth() / 2, screenHeight / 2 - jFrame.getHeight() / 2);  //位于屏幕中央
        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        //添加布局
        JPanel JPanel_main = new JPanel();
        JPanel_main.setLayout(new BorderLayout());
        //文本域
        JTextArea jTextArea = new JTextArea();
        jTextArea.setEditable(false);
        //滚动
        JScrollPane JScrollPane = new JScrollPane(jTextArea);
        JPanel_main.add(JScrollPane, BorderLayout.CENTER);
        JPanel jpanel_bottom = new JPanel();
        jpanel_bottom.setLayout(new BorderLayout());
        JTextField jTextField = new JTextField();
        JButton button = new JButton();
        jpanel_bottom.add(jTextField, BorderLayout.CENTER);
        jpanel_bottom.add(button, BorderLayout.EAST);
        JPanel_main.add(jpanel_bottom, BorderLayout.SOUTH);
        jFrame.add(JPanel_main);

        button.setText("发送消息");
        button.setBackground(Color.cyan);
        Font font = new Font("宋体", Font.BOLD, 36);
        button.setFont(new Font("宋体", Font.BOLD, 24));
        jTextField.setFont(font);
        jTextField.setCaretColor(Color.red);
        jTextField.setDisabledTextColor(Color.cyan);
        jTextArea.setFont(new Font("宋体", Font.BOLD, 24));
        jTextArea.setCaretColor(Color.green);
        jTextField.setForeground(Color.GREEN);
        button.setForeground(Color.MAGENTA);
        jTextArea.setForeground(Color.magenta);
        jTextArea.setBackground(Color.white);

        //----------------------------------------
        //监听器
        jFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                Toolkit.getDefaultToolkit().beep();
                int dialog = JOptionPane.showConfirmDialog(null,
                        "是否退出？", "退出提示", JOptionPane.YES_NO_OPTION);
                if (dialog == JOptionPane.YES_OPTION)
                {
                    System.exit(1);
                }
            }
        });

        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //System.out.println("点击");
                String content = jTextField.getText();
                //System.out.println(message);
                if (content.length() == 0)
                {
                    JOptionPane.showMessageDialog(null, "请输入消息内容");
                    return;
                }
                try
                {
                    String message = queueName + "：" + content;
                    channel_send.basicPublish(EXCHANGE_NAME,
                            "chat", null, message.getBytes(StandardCharsets.UTF_8));
                }
                catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(null,
                            "异常！发送失败！错误内容：\n" + ex.getMessage());
                    return;
                }
                jTextField.setText("");
            }
        });

        jTextField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    String content = jTextField.getText();
                    if (content.length() == 0)
                    {
                        JOptionPane.showMessageDialog(null, "请输入消息内容");
                        return;
                    }
                    try
                    {
                        String message = queueName + "：" + content;
                        channel_send.basicPublish(EXCHANGE_NAME,
                                "chat", null, message.getBytes(StandardCharsets.UTF_8));
                    }
                    catch (IOException ex)
                    {
                        JOptionPane.showMessageDialog(null,
                                "异常！发送失败！错误内容：\n" + ex.getMessage());
                        return;
                    }
                    jTextField.setText("");
                }
            }
        });

        try
        {
            channel_receive.basicConsume(queueName, true, new DeliverCallback()
            {
                @Override
                public void handle(String consumerTag, Delivery message) throws IOException
                {
                    byte[] messageBody = message.getBody();
                    String content = new String(messageBody, StandardCharsets.UTF_8);
                    jTextArea.append(content + "\n");
                }
            }, new CancelCallback()
            {
                @Override
                public void handle(String consumerTag) throws IOException
                {
                    System.out.println(consumerTag);
                }
            });
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null,
                    "接收异常！错误内容：\n" + e.getMessage());
        }

        //显示
        jFrame.setVisible(true);
    }


    public static void main(String[] args)
    {
        new Run();
    }
}
