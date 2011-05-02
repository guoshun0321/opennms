package org.opennms.systemreport.formatters;

import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.Resource;

public class TextSystemReportFormatter extends AbstractSystemReportFormatter implements SystemReportFormatter {

    @Override
    public String getName() {
        return "text";
    }

    @Override
    public String getDescription() {
        return "Simple human-readable indented text";
    }
    
    public String getContentType() {
        return "text/plain";
    }

    public String getExtension() {
        return "txt";
    }

    public boolean canStdout() {
        return true;
    }

    @Override
    public void write(final SystemReportPlugin plugin) {
        if (!hasDisplayable(plugin)) return;
        LogUtils.debugf(this, "write(%s)", plugin.getName());
        try {
            final String title = plugin.getName() + " (" + plugin.getDescription() + "):" + "\n";
            getOutputStream().write(title.getBytes());
            for (final Map.Entry<String,Resource> entry : plugin.getEntries().entrySet()) {
                final Resource value = entry.getValue();
                final boolean displayable = isDisplayable(value);
    
                final String text;
                if (displayable) {
                    text = "\t" + entry.getKey() + ": " + getResourceText(value) + "\n";
                } else {
                    text = "\t" + entry.getKey() + ": " + (value == null? "NULL" : value.getClass().getSimpleName() + " resource is not displayable.  Try using the 'zip' format.") + "\n";
                }
                getOutputStream().write(text.getBytes());
            }
        } catch (Throwable e) {
            LogUtils.errorf(this, e, "Error writing plugin data.");
        }
    }
}
