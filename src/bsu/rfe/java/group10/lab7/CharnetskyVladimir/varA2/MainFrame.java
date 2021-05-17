package bsu.rfe.java.group10.lab7.CharnetskyVladimir.varA2;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;
    private static final int FROM_FIELD_DEFAULT_COLUMNS = 10;
    private static final int TO_FIELD_DEFAULT_COLUMNS = 20;
    private static final int INCOMING_AREA_DEFAULT_ROWS = 10;
    private static final int OUTGOING_AREA_DEFAULT_ROWS = 5;
    private static final int SMALL_GAP = 5;
    private static final int MEDIUM_GAP = 10;
    private static final int LARGE_GAP = 15;
    private static final int SERVER_PORT = 4567;
    private final JTextField textFieldFrom;
    private final JTextField textFieldTo;
    private final JTextArea textAreaIncoming;
    private final JTextArea textAreaOutgoing;
//    private boolean ctrl = false;
//    private boolean enter = false;
//    private boolean cursor = false;

    public MainFrame() {
        super(FRAME_TITLE);
        setMinimumSize(new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));
        // Центрирование окна
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2, (kit.getScreenSize().height - getHeight()) / 2);
        // Текстовая область для отображения полученных сообщений
        textAreaIncoming = new JTextArea(INCOMING_AREA_DEFAULT_ROWS, 0);
        textAreaIncoming.setEditable(false);
        // Контейнер, обеспечивающий прокрутку текстовой области
        final JScrollPane scrollPaneIncoming = new JScrollPane(textAreaIncoming);
        // Подписи полей
        final JLabel labelFrom = new JLabel("Подпись");
        final JLabel labelTo = new JLabel("Получатель");
        // Поля ввода имени пользователя и адреса получателя
        textFieldFrom = new JTextField(FROM_FIELD_DEFAULT_COLUMNS);
        textFieldTo = new JTextField(TO_FIELD_DEFAULT_COLUMNS);
        // Текстовая область для ввода сообщения
        textAreaOutgoing = new JTextArea(OUTGOING_AREA_DEFAULT_ROWS, 0);
        // Контейнер, обеспечивающий прокрутку текстовой области
        final JScrollPane scrollPaneOutgoing = new JScrollPane(textAreaOutgoing);
        // Панель ввода сообщения
        final JPanel messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createTitledBorder("Сообщение"));
        // Кнопка отправки сообщения
        final JButton sendButton = new JButton("Отправить");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

//        //отслеживание событий клавиатуры
//        textAreaOutgoing.addKeyListener(new KeyAdapter() {
//
//            public void keyPressed(KeyEvent e) {
//                int codeKey = e.getKeyCode();
//                if (codeKey == 10) enter = true;
//                if (codeKey == 17) ctrl = true;
//                //Проверка на возможность отправки по условию
//                if(enter && ctrl && cursor) sendMessage();
//            }
//
//            public void keyReleased(KeyEvent e) {
//                int codeKey = e.getKeyCode();
//                if (codeKey == 10) enter = false;
//                if (codeKey == 17) ctrl = false;
//            }
//        });
//
//        //отслеживание курсора в текстовом поле
//        textAreaOutgoing.addMouseListener(new MouseAdapter() {
//            //метод определяющий попадание курсора в компонент
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                cursor = true;
//            }
//            //метод определяющий выход курсора из компонента
//            @Override
//            public void mouseExited(MouseEvent e) {
//                cursor = false;
//            }
//        });

        // Компоновка элементов панели "Сообщение"
        final GroupLayout layout2 = new GroupLayout(messagePanel);
        messagePanel.setLayout(layout2);
        layout2.setHorizontalGroup(layout2.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout2.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(layout2.createSequentialGroup()
                                .addComponent(labelFrom)
                                .addGap(SMALL_GAP)
                                .addComponent(textFieldFrom)
                                .addGap(LARGE_GAP)
                                .addComponent(labelTo)
                                .addGap(SMALL_GAP)
                                .addComponent(textFieldTo))
                        .addComponent(scrollPaneOutgoing)
                        .addComponent(sendButton))
                .addContainerGap());
        layout2.setVerticalGroup(layout2.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout2.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelFrom)
                        .addComponent(textFieldFrom)
                        .addComponent(labelTo)
                        .addComponent(textFieldTo))
                .addGap(MEDIUM_GAP)
                .addComponent(scrollPaneOutgoing)
                .addGap(MEDIUM_GAP)
                .addComponent(sendButton)
                .addContainerGap());
        // Компоновка элементов фрейма
        final GroupLayout layout1 = new GroupLayout(getContentPane());
        setLayout(layout1);
        layout1.setHorizontalGroup(layout1.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout1.createParallelGroup()
                        .addComponent(scrollPaneIncoming)
                        .addComponent(messagePanel))
                .addContainerGap());
        layout1.setVerticalGroup(layout1.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneIncoming)
                .addGap(MEDIUM_GAP)
                .addComponent(messagePanel)
                .addContainerGap());
        // Создание и запуск потока-обработчика запросов
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
                    while (!Thread.interrupted()) {
                        final Socket socket = serverSocket.accept();
                        final DataInputStream in = new DataInputStream(socket.getInputStream());
                        // Читаем имя отправителя
                        final String senderName = in.readUTF();
                        // Читаем сообщение
                        final String message = in.readUTF();
                        //исправил: указываю Дату ПОЛУЧЕНИЯ сообжения
                        //Date date = new Date();
                        //String messageDate = date.toString();
                        // Закрываем соединение
                        socket.close();
                        // Выделяем IP-адрес
                        final String address = ((InetSocketAddress) socket
                                .getRemoteSocketAddress())
                                .getAddress()
                                .getHostAddress();
                        // Выводим сообщение в текстовую область
                        textAreaIncoming.append(senderName + " (" + address + "): " + message + "(Дата получения сообщения: " + /*messageDate*/  " )" + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this, "Ошибка в работе сервера", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();
    }

    private void sendMessage() {
        try {
            // Получаем необходимые параметры
            final String senderName = textFieldFrom.getText();
            final String destinationAddress = textFieldTo.getText();
            final String message = textAreaOutgoing.getText();


            byte[] bytes;
            int[] NumbersAndPoint = {48,49,50,51,52,53,54,55,56,57,46};
            int[] Point_ASCII = {46};
            int[] Symbols_ASCII = {65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122};
            try {
                bytes = destinationAddress.getBytes("US-ASCII");
                int c = 0;
                int Point = 0;
                int Symbols = 0;
                int Signs = 0;
                for(int i = 0; i < bytes.length; i++) {
                    int f = 0;
                    for(int j = 0; j < 11; j++) {
                        if (bytes[i] == NumbersAndPoint[j]){
                            c++;
                            f++;
                        }
                    }
                    for(int j = 0; j < 1; j++) {
                        if (bytes[i] == Point_ASCII[j]){
                            Point++;
                            f++;
                        }
                    }
                    for(int j = 0; j < 50; j++) {
                        if (bytes[i] == Symbols_ASCII[j]){
                            Symbols++;
                            f++;
                        }
                    }
                    if(f == 0){
                        Signs++;
                    }
                }
                if(c != bytes.length){
                    JOptionPane.showMessageDialog(this, "Вы ввели недопустимые значения т.е. отличные от (0-9 и . (точки))", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
                if(Point == 0){
                    JOptionPane.showMessageDialog(this, "Вы не ввели точку(и) (она обязательна при вводе))", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
                if(Symbols != 0){
                    JOptionPane.showMessageDialog(this, "Вы ввели букву(ы) (это недопустимо)", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
                if(Signs != 0){
                    JOptionPane.showMessageDialog(this, "Вы ввели симол(ы) (это недопустимо)", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }catch(UnsupportedEncodingException e){
                System.out.println("Неподдерживаемая кодировка!");
            }
            
            // Убеждаемся, что поля не пустые
            if (senderName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите имя отправителя", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (destinationAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите адрес узла-получателя", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Вы не ввелите текс собщения.Пожалуйста введите", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Создаем сокет для соединения
            final Socket socket = new Socket(destinationAddress, SERVER_PORT);
            // Открываем поток вывода данных
            final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            // Записываем в поток имя
            out.writeUTF(senderName);
            // Записываем в поток сообщение
            out.writeUTF(message);
            // Закрываем сокет
            socket.close();
            // Помещаем сообщения в текстовую область вывода
            textAreaIncoming.append("Я -> " + destinationAddress + ": " + message + "\n");
            // Очищаем текстовую область ввода сообщения
            textAreaOutgoing.setText("");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this, "Не удалось отправить сообщение: узел-адресат не найден", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this, "Не удалось отправить сообщение", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final MainFrame frame = new MainFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }
}