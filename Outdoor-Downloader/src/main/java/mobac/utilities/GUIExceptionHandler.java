/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.utilities;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import mobac.program.Logging;
import mobac.program.ProgramInfo;
import mobac.program.interfaces.ExceptionExtendedInfo;

import org.apache.log4j.Logger;

import com.sleepycat.je.ExceptionEvent;
import com.sleepycat.je.ExceptionListener;

public class GUIExceptionHandler implements Thread.UncaughtExceptionHandler, ExceptionListener {

	private static final GUIExceptionHandler instance = new GUIExceptionHandler();

	private static final Logger log = Logger.getLogger(GUIExceptionHandler.class);

	private static final double MB_DIV = 1024d * 1024d;

	private static final String URL_BUGTRACKER = "http://sourceforge.net/tracker/?group_id=238075&atid=1105494";

	static {
		Thread.setDefaultUncaughtExceptionHandler(instance);
	}

	public static void registerForCurrentThread() {
		Thread t = Thread.currentThread();
		log.trace("Registering MOBAC exception handler for thread \"" + t.getName() + "\" [" + t.getId() + "]");
		t.setUncaughtExceptionHandler(instance);
	}

	public static GUIExceptionHandler getInstance() {
		return instance;
	}

	private GUIExceptionHandler() {
		super();
	}

	/**
	 * Implementation for {@link com.sleepycat.je.ExceptionListener}
	 */
	public void exceptionThrown(ExceptionEvent paramExceptionEvent) {
		Exception e = paramExceptionEvent.getException();
		log.error("Exception in tile store: " + paramExceptionEvent.toString(), e);
		showExceptionDialog(e);
	}

	/**
	 * Implementation for {@link Thread.UncaughtExceptionHandler}
	 */
	public void uncaughtException(Thread t, Throwable e) {
		processException(t, e);
	}

	public static void processException(Throwable e) {
		processException(Thread.currentThread(), e);
	}

	public static void processFatalExceptionSimpleDialog(String dialogMessage, Throwable e) {
		log.error(dialogMessage + ": " + e.getMessage(), e);
		String[] options = { "Exit", "Show error report" };
		int a = JOptionPane.showOptionDialog(null, dialogMessage, "Error", 0, JOptionPane.ERROR_MESSAGE, null, options,
				options[0]);
		if (a == 1)
			GUIExceptionHandler.showExceptionDialog(e);
		System.exit(1);
	}

	public static void processException(Thread thread, Throwable t) {
		log.error("Uncaught exception: ", t);
		showExceptionDialog(thread, t, null);
	}

	public static void processException(Thread thread, Throwable t, AWTEvent newEvent) {
		String eventText = newEvent.toString();
		log.error("Uncaught exception on processing event " + eventText, t);
		if (eventText.length() > 100) {
			String[] parts = eventText.split(",");
			StringWriter sw = new StringWriter(eventText.length() + 20);
			sw.write(parts[0]);
			int len = parts[0].length();
			for (int i = 1; i < parts.length; i++) {
				String s = parts[i];
				if (s.length() + len > 80) {
					sw.write("\n\t");
					len = 0;
				}
				sw.write(s);
			}
			eventText = "Event: " + sw.toString();
		}
		showExceptionDialog(thread, t, eventText);
	}

	public static String prop(String key) {
		String s = System.getProperty(key);
		if (s != null)
			return s;
		else
			return "";
	}

	public static void showExceptionDialog(Throwable t) {
		showExceptionDialog(null, Thread.currentThread(), t, null);
	}

	public static void showExceptionDialog(String message, Throwable t) {
		showExceptionDialog(message, Thread.currentThread(), t, null);
	}

	public static void showExceptionDialog(Thread thread, Throwable t, String additionalInfo) {
		showExceptionDialog(null, thread, t, additionalInfo);
	}

	public static synchronized void showExceptionDialog(String message, Thread thread, Throwable t,
			String additionalInfo) {
		try {
			if (t != null) {
				// Recursive check - in case the Exception was thrown in a Component.paint() method
				// this may lead to an infinite recursion with one message dialog per recursion loop
				String thisClassName = GUIExceptionHandler.class.getName();
				for (StackTraceElement ste : t.getStackTrace()) {
					if (ste.getClassName().startsWith(thisClassName)
							&& "showExceptionDialog".equals(ste.getMethodName())) {
						log.error("Recursive error loop detected - aborting");
						return;
					}
				}
			}

			StringBuilder sb = new StringBuilder(2048);
			if (message != null)
				sb.append("Error message: " + message + "\n");
			sb.append("Version: " + ProgramInfo.getCompleteTitle());
			sb.append("\nPlatform: " + prop("os.name") + " (" + prop("os.version") + ")");
			String windowManager = System.getProperty("sun.desktop");
			if (windowManager != null)
				sb.append(" (" + windowManager + ")");

			String dist = OSUtilities.getLinuxDistributionName();
			if (dist != null)
				sb.append("\nDistribution name: " + dist);

			sb.append("\nJava VM: " + prop("java.vm.name") + " (" + prop("java.runtime.version") + ")");
			if (t != null && t.getClass().equals(java.lang.OutOfMemoryError.class)) {
				Runtime r = Runtime.getRuntime();
				sb.append(String.format("\nMax heap size: %3.2f MiB", r.maxMemory() / MB_DIV));
			}
			// sb.append("\nMapsources rev: " + MapSourcesUpdater.getCurrentMapSourcesRev());

			sb.append("\nCPU cores: " + Runtime.getRuntime().availableProcessors());

			if (thread != null)
				sb.append("\n\nThread: " + thread.getName());

			if (additionalInfo != null)
				sb.append("\n\n" + additionalInfo);

			if (t instanceof ExceptionExtendedInfo) {
				ExceptionExtendedInfo ei = (ExceptionExtendedInfo) t;
				sb.append("\n");
				sb.append(ei.getExtendedInfo());
			}

			String guiText;
			String dialogTitle;
			JPanel panel = new JPanel(new BorderLayout());
			if (t != null) {
				sb.append("\n\nError hierarchy:");
				Throwable tmp = t;
				while (tmp != null) {
					sb.append("\n  " + tmp.getClass().getSimpleName() + ": " + tmp.getMessage());
					tmp = tmp.getCause();
				}

				String exceptionName = t.getClass().getSimpleName();
				dialogTitle = "Unexpected Exception: " + exceptionName;
				StringWriter stack = new StringWriter();
				t.printStackTrace(new PrintWriter(stack));
				sb.append("\n\n#############################################################\n\n");
				sb.append(stack.getBuffer().toString());
				sb.append("\n#############################################################");

				guiText = "An unexpected exception occurred (" + exceptionName + ")<br>"
						+ "<p>Please report a ticket in the bug tracker " + "on <a href=\"" + URL_BUGTRACKER
						+ "\">SourceForge.net</a><br>"
						+ "<b>Please include a detailed description of your performed actions <br>"
						+ "before the error occurred.</b></p>Be sure to include the following information:";
			} else {
				dialogTitle = "System report";
				guiText = "This is a user generated system report.<br>"
						+ "<p>It can be used for providing detailed version information <br>"
						+ "in case a new ticket in the bug tracker is created " + "on <a href=\"" + URL_BUGTRACKER
						+ "\">SourceForge.net</a><br>"
						+ "<b>Please include a detailed description of your problem <br>"
						+ "and what steps are necessary to reproduce the problem.</b>" + "</p>";
			}
			JEditorPane text = new JEditorPane("text/html", "");
			text.setOpaque(true);
			text.setBackground(UIManager.getColor("JFrame.background"));
			text.setEditable(false);
			text.addHyperlinkListener(new HyperlinkListener() {

				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
						return;
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (Exception e1) {
						log.error("", e1);
					}
				}
			});
			panel.add(text, BorderLayout.NORTH);
			try {
				StringSelection contents = new StringSelection(sb.toString());
				ClipboardOwner owner = new ClipboardOwner() {
					public void lostOwnership(Clipboard clipboard, Transferable contents) {
					}
				};
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, owner);
				guiText += "<p>(The following text has already been copied to your clipboard.)</p>";
			} catch (RuntimeException x) {
				log.error("", x);
			}
			text.setText("<html>" + guiText + "</html>");

			JCheckBox quickMOBACcb = new JCheckBox("Do not continue and quit program by pressing OK");
			JTextArea info = new JTextArea(sb.toString(), 20, 60);
			info.setCaretPosition(0);
			info.setEditable(false);
			info.setMinimumSize(new Dimension(200, 150));
			panel.add(new JScrollPane(info), BorderLayout.CENTER);
			panel.add(quickMOBACcb, BorderLayout.SOUTH);
			panel.setMinimumSize(new Dimension(700, 300));
			panel.validate();
			JOptionPane.showMessageDialog(null, panel, dialogTitle, JOptionPane.ERROR_MESSAGE);
			if (quickMOBACcb.isSelected()) {
				log.warn("User selected to quit MOBAC after an exception");
				System.exit(1);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void installToolkitEventQueueProxy() {
		EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		queue.push(new EventQueueProxy());
	}

	/**
	 * Catching all Runtime Exceptions in Swing
	 * 
	 * http://ruben42.wordpress.com/2009/03/30/catching-all-runtime-exceptions- in-swing/
	 */
	protected static class EventQueueProxy extends EventQueue {

		protected void dispatchEvent(AWTEvent newEvent) {
			try {
				super.dispatchEvent(newEvent);
			} catch (Throwable e) {
				if (e instanceof ArrayIndexOutOfBoundsException) {
					StackTraceElement[] st = e.getStackTrace();
					if (st.length > 0) {
						if ("sun.font.FontDesignMetrics".equals(st[0].getClassName())) {
							log.error("Ignored JRE bug exception " + e.getMessage() + " caused by : " + st[0]);
							// This is a known JRE bug - we just ignore it
							return;
						}
					}
				}
				GUIExceptionHandler.processException(Thread.currentThread(), e);
			}
		}
	}

	public static void main(String[] args) {
		for (;;) {
			try {
				Logging.configureConsoleLogging();
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				throw new RuntimeException("Test", new Exception("Inner"));
			} catch (Exception e) {
				showExceptionDialog("Test 123",e);
			} catch (Error e) {
				showExceptionDialog(e);
			}
			break;
		}
	}

}
