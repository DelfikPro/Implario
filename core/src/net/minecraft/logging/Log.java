package net.minecraft.logging;

import net.minecraft.server.Todo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Log {

	public static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss");
	public static final SimpleDateFormat DAY = new SimpleDateFormat("yyyy.MM.dd");
	public static final Log MAIN = Log.create(true, "main", "");
	public static final Log SOUND = Log.create(false, "sound", "[ЗВУК] ");
	public static final Log CHAT = Log.create(true, "chat", "[ЧАТ] ");

	private final boolean console;
	private final String extension, prefix;
	protected File file;
	private OutputStreamWriter stream;
	private volatile int day;
	private List<ILogInterceptor> interceptors = new ArrayList<>();

	public static Log create(boolean console, String file, String prefix) {
		try {
			return new Log(console, file, prefix);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Log(boolean console, String extension, String prefix) throws IOException {
		this.console = console;
		this.extension = extension == null ? "log" : extension;
		this.prefix = prefix;

		Date date = new Date();
		updateFile(date);
		comment("Начало новой сессии " + Log.DAY.format(date) + " в " + Log.TIME.format(date));
	}

	public void error(String s) {
		log(s, LogLevel.ERROR);
	}

	public void error(String s, Throwable t){
		error(s);
		error(parseException(t));
	}

	public void warn(String s) {
		log(s, LogLevel.WARNING);
	}

	public void warn(String s, Throwable t){
		warn(s);
		warn(parseException(t));
	}

	public void debug(String s) {
		if (Todo.instance.debugEnabled()) log(s, LogLevel.DEBUG);
	}

	public void debug(String s, Throwable t){
		debug(s);
		debug(parseException(t));
	}

	public void info(String s) {
		log(s, LogLevel.INFO);
	}

	public void comment(String s) {
		log(s, LogLevel.COMMENT);
	}

	public void log(String s, LogLevel level) {
		if (console) System.out.println(prefix + (level == LogLevel.INFO ? "" : "[" + level.name() + "] ") + s);
		Date date = new Date();
		for (ILogInterceptor interceptor : interceptors) {
			interceptor.intercept(level, date, s);
		}
		try {
			updateFile(date);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String line = TIME.format(date) + " " + level.getPrefix() + " " + s + '\n';
		append(line);
	}

	public void close() {
		try {
			stream.flush();
			stream.close();
		} catch (IOException e) {
			System.out.println("[Log] Unable to close stream for " + prefix);
			e.printStackTrace();
		}
	}

	public void addAccessor(ILogInterceptor interceptor) {
		this.interceptors.add(interceptor);
	}

	private String parseException(Throwable t){
		StringBuilder buffer = new StringBuilder();
		buffer.append("* Описание ошибки, которое желательно отправить разработчикам.\n");
		boolean causedBy = false;
		while (t != null) {
			String word = causedBy ? "Reason: " : "Exception: ";
			buffer.append("* " + word + t.getClass().getName() + ". Description: " + t.getMessage() + "\n");
			causedBy = true;
			for (StackTraceElement e : t.getStackTrace()) {
				String file = e.getFileName();
				int line = e.getLineNumber();

				String f;
				if (e.isNativeMethod()) f = "(Native method)";
				else if (file != null && line >= 0) f = "(" + file + ":" + line + ")";
				else f = file != null ? "(" + file + ")" : "(Unknown source)";
				buffer.append("*     " + e.getClassName() + "." + e.getMethodName() + " " + f + "\n");
			}
			t = t.getCause();
		}
		return buffer.toString();
	}

	private void append(String s) {
		try {
			stream.write(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateFile(Date date) throws IOException {
		int today = (int) (date.getTime() / 3600000 / 24);
		if (today <= day) return;
		File f = getFile(date);
		if (f.equals(file)) return;
		day = today;
		boolean continued = false;
		if (stream != null) {
			continued = true;
			append("-- Продолжение следует...\n");
			close();
		}
		file = f;
		stream = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8);
		if (continued) append("-- Продолжение предыдущей сессии\n");
	}

	private File getFile(Date date) throws IOException {
		String name = extension + "_" + DAY.format(date) + ".log";
		File logsDir = new File("gamedata/logs");
		if (!logsDir.exists()) {
			logsDir.mkdirs();
			logsDir.mkdir();
		}
		File f = new File(logsDir, name);
		if (!f.exists()) f.createNewFile();
		return f;
	}
}
