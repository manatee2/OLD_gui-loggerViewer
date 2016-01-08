package manatee.demo.loggerdemo.viewer.shared;

import java.io.Serializable;
import java.util.Date;


//
// TODO - This class is a duplication of LogMessage and LogMessageSeverity from the LoggerDemo project; these should be
// combined into a single shared project.
public class LogMessage implements Serializable
{
    private static final long serialVersionUID = 1L;


    public enum Severity
    {
        Debug, Info, Warning, Error, Fatal
    };

    private int id;
    private Date timestamp;
    private String reporter;
    private Severity severity;
    private String text;


    public LogMessage()
    {
        super();
    }


    public LogMessage(int id, Date timestamp, String reporter, Severity severity, String text)
    {
        super();
        this.id = id;
        this.timestamp = timestamp;
        this.reporter = reporter;
        this.severity = severity;
        this.text = text;
    }


    public int getId()
    {
        return id;
    }


    public void setId(int id)
    {
        this.id = id;
    }


    public Date getTimestamp()
    {
        return timestamp;
    }


    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }


    public String getReporter()
    {
        return reporter;
    }


    public void setReporter(String reporter)
    {
        this.reporter = reporter;
    }


    public Severity getSeverity()
    {
        return severity;
    }


    public void setSeverity(Severity severity)
    {
        this.severity = severity;
    }


    public String getText()
    {
        return text;
    }


    public void setText(String text)
    {
        this.text = text;
    }

}
