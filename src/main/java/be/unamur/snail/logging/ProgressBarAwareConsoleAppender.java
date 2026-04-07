package be.unamur.snail.logging;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

/**
 * Log4j2 console appender that cooperates with {@link ProgressBar}.
 *
 * <p>When a progress bar is running, every log line is routed through
 * {@link ProgressBar#printAbove(String)}, which atomically erases the bar,
 * prints the message, and redraws the bar below it — keeping the bar as a
 * single, stable bottom line instead of being interleaved with log output.
 *
 * <p>When no bar is active the message is written to {@code System.out}
 * unchanged, just as the standard {@code Console} appender would.
 *
 * <p>Registered as a Log4j2 plugin so that {@code log4j2.xml} can reference
 * it by the element name {@code <ProgressBarAwareConsole>}.  The hosting
 * {@code log4j2.xml} must declare
 * {@code packages="be.unamur.snail.logging"} on its {@code <Configuration>}
 * element so that Log4j2 discovers the plugin at startup.
 */
@Plugin(name = "ProgressBarAwareConsole",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true)
public final class ProgressBarAwareConsoleAppender extends AbstractAppender {

    private ProgressBarAwareConsoleAppender(String name,
                                            Filter filter,
                                            Layout<? extends Serializable> layout,
                                            boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static ProgressBarAwareConsoleAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filters") Filter filter) {
        if (name == null) {
            LOGGER.error("ProgressBarAwareConsoleAppender requires a name");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new ProgressBarAwareConsoleAppender(name, filter, layout, true);
    }

    /**
     * Formats the event with the configured layout and either routes it through
     * the active progress bar (so the bar stays as a single bottom line) or
     * prints it directly to {@code System.out} when no bar is running.
     *
     * <p>The layout pattern should <em>not</em> end with {@code %n}: both
     * {@link ProgressBar#printAbove(String)} and {@code System.out.println}
     * append their own newline.
     */
    @Override
    public void append(LogEvent event) {
        // stripTrailing() removes the trailing newline added by the layout's %n
        // so that printAbove / println each contribute exactly one newline.
        String message = new String(getLayout().toByteArray(event)).stripTrailing();
        ProgressBar active = ProgressBar.getActive();
        if (active != null) {
            active.printAbove(message);
        } else {
            System.out.println(message);
        }
    }
}

