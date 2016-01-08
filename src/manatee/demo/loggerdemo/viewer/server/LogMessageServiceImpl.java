package manatee.demo.loggerdemo.viewer.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.core.client.util.DateWrapper;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

import manatee.demo.loggerdemo.viewer.client.LogMessageService;
import manatee.demo.loggerdemo.viewer.shared.LogMessage;
import manatee.demo.loggerdemo.viewer.shared.LogMessage.Severity;


public class LogMessageServiceImpl extends RemoteServiceServlet implements LogMessageService
{
    private static final long serialVersionUID = 1L;

    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/manateedb";
    private static final String DATABASE_USER = "manateeuser";
    private static final String DATABASE_PASSWORD = "manateepassword";


    @Override
    public PagingLoadResult<LogMessage> getLogMessages(FilterPagingLoadConfig config)
    {
        try
        {
            //
            // Open a JDBC Connection.
            //
            Connection jdbcConnection =
                    DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

            //
            // Create a JDBC Statement.
            //
            Statement jdbcStatement = jdbcConnection.createStatement();

            //
            // Fetch ALL the rows.
            //
            List<LogMessage> allLogMessages = new ArrayList<LogMessage>();
            String query = "SELECT id, timestamp, reporter, severity, text FROM log_messages ORDER BY id ASC";
            ResultSet resultSet = jdbcStatement.executeQuery(query);
            while (resultSet.next())
            {
                //
                // Extract each column by name.
                //
                int id = resultSet.getInt("id");
                Date timestamp = resultSet.getTimestamp("timestamp");
                String reporter = resultSet.getString("reporter");
                String severity = resultSet.getString("severity");
                String text = resultSet.getString("text");

                LogMessage logMessage = new LogMessage(id, timestamp, reporter, Severity.valueOf(severity), text);
                allLogMessages.add(logMessage);
            }

            //
            // Sort the rows.
            //
            if (config.getSortInfo().size() > 0)
            {
                SortInfo sort = config.getSortInfo().get(0);
                if (sort.getSortField() != null)
                {
                    final String sortField = sort.getSortField();
                    if (sortField != null)
                    {
                        Collections.sort(allLogMessages, sort.getSortDir().comparator(new Comparator<LogMessage>()
                        {
                            @Override
                            public int compare(LogMessage logMessage1, LogMessage logMessage2)
                            {
                                if (sortField.equals("id"))
                                {
                                    return logMessage1.getId() - logMessage2.getId();
                                }
                                if (sortField.equals("timestamp"))
                                {
                                    return logMessage1.getTimestamp().compareTo(logMessage2.getTimestamp());
                                }
                                if (sortField.equals("reporter"))
                                {
                                    return logMessage1.getReporter().compareTo(logMessage2.getReporter());
                                }
                                if (sortField.equals("severity"))
                                {
                                    return logMessage1.getSeverity().compareTo(logMessage2.getSeverity());
                                }
                                if (sortField.equals("text"))
                                {
                                    return logMessage1.getText().compareTo(logMessage2.getText());
                                }
                                return 0;
                            }
                        }));
                    }
                }
            }

            //
            // Filter-Out any undesired entries.
            //
            List<LogMessage> toBeRemoved = new ArrayList<LogMessage>();
            List<FilterConfig> filters = config.getFilters();
            for (FilterConfig filter : filters)
            {
                String type = filter.getType();
                String test = filter.getValue();
                String path = filter.getField();
                String comparison = filter.getComparison();

                String safeTest = test == null ? "" : test.toString();

                // System.out.println("type = " + type + ", test = " + test + ", path = " + path + ", comparision = " +
                // comparison + ", safeTest = " + safeTest);

                for (LogMessage logMessage : allLogMessages)
                {
                    String value = getLogMessageValue(logMessage, path);
                    String safeValue = value == null ? null : value.toString();

                    if (safeTest.length() == 0 && (safeValue == null || safeValue.length() == 0))
                    {
                        continue;
                    }
                    else if (safeValue == null)
                    {
                        toBeRemoved.add(logMessage);
                        continue;
                    }

                    if ("string".equals(type))
                    {
                        if (safeValue.toLowerCase().indexOf(safeTest.toLowerCase()) == -1)
                        {
                            toBeRemoved.add(logMessage);
                        }
                    }
                    else if ("date".equals(type))
                    {
                        if (isDateFiltered(safeTest, comparison, safeValue))
                        {
                            toBeRemoved.add(logMessage);
                        }
                    }
                    else if ("list".equals(type))
                    {
                        if (isListFiltered(safeTest, safeValue))
                        {
                            toBeRemoved.add(logMessage);
                        }
                    }
                }

            }
            for (LogMessage logMessage : toBeRemoved)
            {
                allLogMessages.remove(logMessage);
            }

            //
            // Extract the desired page.
            //
            ArrayList<LogMessage> desiredPage = new ArrayList<LogMessage>();
            int start = config.getOffset();
            int limit = allLogMessages.size();
            if (config.getLimit() > 0)
            {
                limit = Math.min(start + config.getLimit(), limit);
            }
            for (int i = config.getOffset(); i < limit; i++)
            {
                desiredPage.add(allLogMessages.get(i));
            }

            return new PagingLoadResultBean<LogMessage>(desiredPage, allLogMessages.size(), config.getOffset());
        }
        catch (SQLException exception)
        {
            exception.printStackTrace();
        }
        return null;
    }


    private String getLogMessageValue(LogMessage logMessage, String property)
    {
        if (property.equals("timestamp"))
        {
            return String.valueOf(logMessage.getTimestamp().getTime());
        }
        else if (property.equals("severity"))
        {
            return logMessage.getSeverity().toString();
        }
        else if (property.equals("reporter"))
        {
            return logMessage.getReporter();
        }
        else if (property.equals("text"))
        {
            return String.valueOf(logMessage.getText());
        }

        return "";
    }


    private boolean isDateFiltered(String test, String comparison, String value)
    {
        Date t = new Date(Long.valueOf(test));
        Date v = new Date(Long.valueOf(value));
        if (value == null)
        {
            return false;
        }
        if ("after".equals(comparison))
        {
            return v.before(t);
        }
        else if ("before".equals(comparison))
        {
            return v.after(t);
        }
        else if ("on".equals(comparison))
        {
            t = new DateWrapper(t).resetTime().asDate();
            v = new DateWrapper(v).resetTime().asDate();
            return !v.equals(t);
        }
        return true;
    }


    private boolean isListFiltered(String test, String value)
    {
        String[] tests = test.split("::");
        for (int i = 0; i < tests.length; i++)
        {
            if (tests[i].equals(value))
            {
                return false;
            }
        }
        return true;
    }

}
