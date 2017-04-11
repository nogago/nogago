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
package mobac.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import mobac.program.tilestore.berkeleydb.DelayedInterruptThread;
import mobac.utilities.Utilities;

import org.apache.log4j.Logger;

public class WorkinprogressDialog extends JDialog implements WindowListener {

	private static final Logger log = Logger.getLogger(WorkinprogressDialog.class);

	private final ThreadFactory threadFactory;
	private Thread workerThread;

	public WorkinprogressDialog(Frame owner, String title) {
		this(owner, title, Executors.defaultThreadFactory());
	}

	public WorkinprogressDialog(Frame owner, String title, ThreadFactory threadFactory) {
		super(owner, title, true);
		this.threadFactory = threadFactory;
		setLayout(new FlowLayout());
		add(new JLabel(new ImageIcon(Utilities.getResourceImageUrl("ajax-loader.gif"))));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(owner);
		addWindowListener(this);
		JButton abort = new JButton("Abort");
		abort.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				log.debug("User interrupted process");
				WorkinprogressDialog.this.close();
			}
		});
		add(abort);
		pack();
	}

	public void startWork(final Runnable r) {
		workerThread = threadFactory.newThread(new Runnable() {

			public void run() {
				try {
					r.run();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				} finally {
					WorkinprogressDialog.this.close();
					log.debug("Worker thread finished");
				}
			}

		});
		Thread t1 = new Thread() {
			@Override
			public void run() {
				setVisible(true);
			}
		};
		t1.start();
	}

	protected synchronized void abortWorking() {
		try {
			if (workerThread != null && !workerThread.isInterrupted()) {
				log.debug("User aborted process - interrupting worker thread");
				workerThread.interrupt();
				workerThread = null;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void close() {
		abortWorking();
		setVisible(false);
	}

	public void windowActivated(WindowEvent event) {
	}

	public void windowOpened(WindowEvent event) {
		workerThread.start();
	}

	public void windowClosed(WindowEvent event) {
		abortWorking();
	}

	public void windowClosing(WindowEvent event) {
	}

	public void windowDeactivated(WindowEvent event) {
	}

	public void windowDeiconified(WindowEvent event) {
	}

	public void windowIconified(WindowEvent event) {
	}

	public static void main(String[] args) {
		JFrame parentFrame = new JFrame();
		parentFrame.setSize(500, 150);
		final JLabel jl = new JLabel();
		jl.setText("Count : 0");

		parentFrame.add(BorderLayout.CENTER, jl);
		parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		parentFrame.setVisible(true);

		final WorkinprogressDialog dlg = new WorkinprogressDialog(parentFrame, "Progress",
				DelayedInterruptThread.createThreadFactory());

		final Thread t = new Thread() {

			@Override
			public void run() {
				try {
					for (int i = 0; i <= 500; i++) {
						jl.setText("Count : " + i);
						if (Thread.currentThread().isInterrupted()) {
							System.out.println("Aborted");
							return;
						}
						Thread.sleep(25);
					}
				} catch (InterruptedException e) {
					System.out.println("Aborted");
					return;
				} finally {
					dlg.setVisible(false);
				}
			}

		};
		dlg.startWork(t);
	}
}
