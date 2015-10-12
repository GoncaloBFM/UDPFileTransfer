package t1;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gbfm on 10/12/15.
 */
public class Alarm {

	private Timer timer;
	private HashMap<Long, TaskThread> tasks;

	public Alarm() {
		this.timer = new Timer();
		tasks = new HashMap<Long, TaskThread>();
	}

	/**
	 * Adds a task to the alarm
	 * @param id Id of the new task
	 * @param millisecondDelay Time until the tasks executes
	 * @param task Task to execute when the time goes off
	 * @return True if the task was set, false if not. The task is not set if the given task id already exists
	 */
	public boolean schedule(long id, long millisecondDelay, Task task) {
		TaskThread taskThread = new TaskThread(id, task);

		if (this.tasks.get(id) != null) {
			return false;
		}
		this.tasks.put(id, taskThread);
		this.timer.schedule(taskThread, millisecondDelay);

		return true;
	}

	/**
	 * Removes a task from the alarm
	 * @param id Id of the task to remove
	 * @return True if the task was removed, false if not. The task is not removed if the given id does not exist
	 */
	public boolean unschedule(long id) {
		TaskThread task = this.tasks.get(id);
		if (task != null) {
			task.cancel();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the number of scheduled tasks
	 * @return The number of scheduled tasks
	 */
	public int getNumberOfCurrentTasks() {
		return this.tasks.size();
	}

	/**
	 * Cancels all scheduled tasks
	 * Needs to be executed before the program terminates
	 */
	public void cancelAll() {
		this.timer.cancel();
	}

	private class TaskThread extends TimerTask {

		private Task task;
		private long id;

		public TaskThread(long id, Task task) {
			super();
			this.id = id;
			this.task = task;
		}

		public void run() {
			Alarm.this.tasks.remove(id);
			this.task.excute();
		}
	}

	public interface Task {
		void excute() throws IOException;
	}

}
