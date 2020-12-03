package burp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.handshake.ServerHandshake;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class BurpExtender extends AbstractTableModel implements IBurpExtender, ITab {
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private JSplitPane splitPane;
    private JTextField url;
    private IMessageEditor requestViewer;
    private IMessageEditor responseViewer;
    private IHttpRequestResponse currentlyDisplayedItem;
    private URI uri;
    private BurpCollaboratorThread thread;
    private JTabbedPane parent;
    private Table logTable;
    private final List<JsonObject> log2 = Collections.synchronizedList(new ArrayList<JsonObject>());

    //
    // implement IBurpExtender
    //


    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks) {
        // keep a reference to our callbacks object
        this.callbacks = callbacks;


        // obtain an extension helpers object
        helpers = callbacks.getHelpers();

        // set our extension name
        callbacks.setExtensionName("Reflector Burp");

        // create our UI
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                parent = new JTabbedPane();
                logTable = new Table(BurpExtender.this);

                //config

                JPanel config = new JPanel();
                JLabel label = new JLabel("API Websocket:");
                final JTextField url = new JTextField("APIWebSocket URL");
                url.setPreferredSize(new Dimension(500, 30));
                JButton button = new JButton("Connect");
                final JButton button1 = new JButton("Disconnect");
                button1.setEnabled(false);
                final JLabel state = new JLabel("Any connections");
                config.add(label);
                config.add(url);
                config.add(button);
                config.add(button1);
                config.add(state);

                button.addActionListener(new ActionListener() {
                    BurpCollaboratorThread th;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            state.setText("Connected ...");
                            th = new BurpCollaboratorThread(new URI(url.getText()), new WebSocketConnectionCallback() {
                                @Override
                                public void onOpen(ServerHandshake handshakedata) {
                                    System.out.println("Connect");
                                    //Check connect DB
                                    state.setText("Connect");
                                }

                                @Override
                                public void onMessage(String message) {
                                    System.out.println(message);
                                    System.out.println("Refreshing list2");
                                    synchronized (log2) {
                                        int row = log2.size();
                                        log2.add(new JsonParser().parse(message).getAsJsonObject());
                                        BurpExtender.this.fireTableRowsInserted(row, row);
                                    }
                                }

                                @Override
                                public void onClose(int code, String reason, boolean remote) {
                                    System.out.println("Cerrado");

                                }

                                @Override
                                public void onError(Exception ex) {
                                    System.out.println(ex);
                                }

                            });
                        } catch (URISyntaxException error) {
                            error.printStackTrace();
                        }
                        thread = th;
                        thread.start();
                        button.setEnabled(false);
                        button1.setEnabled(true);
                    }
                });


                button1.addActionListener(new ActionListener() {
                                              @Override
                                              public void actionPerformed(ActionEvent e) {
                                                  thread.interrupt();
                                                  state.setText("Disconnect");
                                                  button.setEnabled(true);
                                                  button1.setEnabled(false);
                                              }
                                          }
                );

                // main split pane
                splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

                // table of log entries
                JScrollPane scrollPane = new JScrollPane(logTable);

                splitPane.setLeftComponent(scrollPane);

                // tabs with request/response viewers
                JTabbedPane tabs = new JTabbedPane();
                requestViewer = callbacks.createMessageEditor(null, false);
                //  responseViewer = callbacks.createMessageEditor((IMessageEditorController) BurpExtender.this, false);
                tabs.addTab("Request", requestViewer.getComponent());
                // tabs.addTab("Response", responseViewer.getComponent());
                splitPane.setRightComponent(tabs);


                parent.addTab("Proxy", splitPane);
                parent.addTab("Config", config);

                // customize our UI components
                callbacks.customizeUiComponent(splitPane);
                callbacks.customizeUiComponent(logTable);
                callbacks.customizeUiComponent(scrollPane);
                callbacks.customizeUiComponent(tabs);

                // add the custom tab to Burp's UI
                callbacks.addSuiteTab(BurpExtender.this);


            }
        });

    }

    //
    // implement ITab
    //

    @Override
    public String getTabCaption() {
        return "Burp-Reflector";
    }

    @Override
    public Component getUiComponent() {
        return parent;
    }


    //
    // extend AbstractTableModel
    //

    @Override
    public int getRowCount() {
        System.out.println("Getting list size");
        System.out.println(log2.size());
        return log2.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "#";
            case 1:
                return "Time";
            case 2:
                return "From IP";
            case 3:
                return "Method";
            case 4:
                return "Host";
            default:
                return "Path";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        JsonObject obj = log2.get(rowIndex);
        System.out.println("Getting value");
        switch (columnIndex) {
            case 0:
                return rowIndex;
            case 1:
                return obj.getAsJsonObject("requestContext").get("time").getAsString();
            case 2:
                return obj.getAsJsonObject("requestContext").getAsJsonObject("http").get("sourceIp").getAsString();
            case 3:
                return obj.getAsJsonObject("requestContext").getAsJsonObject("http").get("method").getAsString();
            case 4:
                return obj.getAsJsonObject("requestContext").get("domainName").getAsString();
            default:
                return obj.getAsJsonObject("requestContext").getAsJsonObject("http").get("path").getAsString();
        }

    }


    //
    // extend JTable to handle cell selection
    //

    private class Table extends JTable {
        public Table(TableModel tableModel) {
            super(tableModel);
        }

        @Override
        public void changeSelection(int row, int col, boolean toggle, boolean extend) {
            // show the log entry for the selected row
            /*LogEntry logEntry = log2.get(row);

            requestViewer.setMessage(logEntry.requestResponse.getRequest(), true);
            responseViewer.setMessage(logEntry.requestResponse.getResponse(), false);
            currentlyDisplayedItem = logEntry.requestResponse;

            super.changeSelection(row, col, toggle, extend);*/


            String request = "";
            JsonObject req = log2.get(row);
            request += req.getAsJsonObject("requestContext").getAsJsonObject("http").get("method").getAsString() + " ";
            request += req.getAsJsonObject("requestContext").getAsJsonObject("http").get("path").getAsString() + " ";
            request += req.getAsJsonObject("requestContext").getAsJsonObject("http").get("protocol").getAsString() + "\n";
            JsonObject jsonObject = req.getAsJsonObject("headers");
            Set<String> keys = jsonObject.keySet();
            for (String key : keys) {
                request += key + ": " + jsonObject.get(key).getAsString() + "\n";
            }
            request += "\n";

            if (req.has("body")) {
                request += new String(Base64.getDecoder().decode(req.get("body").getAsString()));
            }

            requestViewer.setMessage(request.getBytes(), true);

            super.changeSelection(row, col, toggle, extend);
        }
    }

    //
    // class to hold details of each log entry
    //

    static class LogEntry {
        final int tool;
        final IHttpRequestResponsePersisted requestResponse;
        final URL url;

        LogEntry(int tool, IHttpRequestResponsePersisted requestResponse, URL url) {
            this.tool = tool;
            this.requestResponse = requestResponse;
            this.url = url;
        }
    }
}