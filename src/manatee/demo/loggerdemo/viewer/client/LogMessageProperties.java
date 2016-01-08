package manatee.demo.loggerdemo.viewer.client;

import java.util.Date;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import manatee.demo.loggerdemo.viewer.shared.LogMessage;
import manatee.demo.loggerdemo.viewer.shared.LogMessage.Severity;


public interface LogMessageProperties extends PropertyAccess<LogMessage>
{
    @Path("id")
    ModelKeyProvider<LogMessage> key();


    ValueProvider<LogMessage, Integer> id();


    ValueProvider<LogMessage, String> reporter();


    ValueProvider<LogMessage, Date> timestamp();


    ValueProvider<LogMessage, Severity> severity();


    ValueProvider<LogMessage, String> text();
}
