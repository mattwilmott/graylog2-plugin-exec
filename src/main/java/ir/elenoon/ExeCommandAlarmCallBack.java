package ir.elenoon;

import java.io.IOException;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import org.graylog2.plugin.Message;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.AlertCondition.CheckResult;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.streams.Stream;

/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class ExeCommandAlarmCallBack implements AlarmCallback{
	private Configuration configs;

	@Override
	public void call(Stream stream, CheckResult result)
			throws AlarmCallbackException {
		try {
			LinkedList<String> envp = new LinkedList<String>();
			envp.add("streamId=" + stream.getId());
			envp.add("streamTitle="+stream.getTitle());
      			envp.add("streamDescription=" + stream.getDescription());
        		envp.add("streamString=" + stream.toString());
       			envp.add("AlarmDescription=" + result.getResultDescription());
       			envp.add("AlarmString=" + result.toString());
       			envp.add("AlarmTriggeredAt=" + result.getTriggeredAt().toString());
       			envp.add("AlarmTriggeredConditionId=" + result.getTriggeredCondition().getId());
       			envp.add("AlarmTriggeredConditionBacklog=" + result.getTriggeredCondition().getBacklog());
       			envp.add("AlarmTriggeredConditionTypeString=" + result.getTriggeredCondition().getTypeString());
       			envp.add("AlarmTriggeredConditionCreatedAt=" + result.getTriggeredCondition().getCreatedAt().toString());
       			envp.add("AlarmTriggeredConditionDescription=" + result.getTriggeredCondition().getDescription());   
			AlertCondition alertCondition = result.getTriggeredCondition();
		        List<Message> messages = alertCondition.getSearchHits();
       			Message message = null;
            		if(0 < messages.size())
            		{
                		message = messages.get(0);
				envp.add("MessageSource=" + message.getField("source"));   
				envp.add("MessageFull=" + message.getField("full_message"));   
            		}
	
			Runtime.getRuntime().exec(new String[]{"bash","-c",configs.getString("bashCommand")},envp.toArray(new String[envp.size()]));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		String command = configs.getString("bashCommand");
		if (command.isEmpty())
			throw new ConfigurationException("Fill the bash command field.");
			 
	}

	@Override
	public Map<String, Object> getAttributes() {
		return configs.getSource();
	}

	@Override
	public String getName() {
		return "Execute Command Alarm Callback";
	}

	@Override
	public ConfigurationRequest getRequestedConfiguration() {
		final ConfigurationRequest configurationRequest = new ConfigurationRequest();
		configurationRequest.addField(new TextField("bashCommand", "Bash Command", "", "", ConfigurationField.Optional.NOT_OPTIONAL));
		return configurationRequest;
	}

	@Override
	public void initialize(Configuration arg0)
			throws AlarmCallbackConfigurationException {
		configs = new Configuration(arg0.getSource());
	}
	
}
