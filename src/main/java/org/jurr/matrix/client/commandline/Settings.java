package org.jurr.matrix.client.commandline;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.beust.jcommander.Parameter;

public final class Settings
{
	private static final String DEFAULT_CONFIG_FILE = "~/.matrix-cmdline.conf";
	private static final int UNLIMITED_MAX_CHARS = -1;

	@Parameter(names = { "--help", "-h" }, description = "Show this help message", help = true)
	private boolean help;

	@Parameter(names = { "--log", "-l", "--verbose", "-v" }, description = "Verbose output")
	private boolean verboseOutput = false;

	@Parameter(names = { "--server", "-s" }, description = "Hostname to connect to", required = true)
	private String server = "https://matrix.org";

	@Parameter(names = { "--username", "--user", "-u" }, description = "Username to use for login", required = true)
	private String username;

	@Parameter(names = { "--password", "--pass", "-p" }, description = "Password to use for login", required = true, password = true, echoInput = false)
	private String password;

	@Parameter(names = { "--room", "-r" }, description = "Room ID or alias to send message to", required = true)
	private String roomIdOrAlias;

	@Parameter(names = { "--configuration-file", "--config-file", "-c" }, description = "Configuration file")
	private String configurationFile;
	private Path configurationFilePath;

	@Parameter(names = { "--max-chars" }, description = "Maximum number of characters to send into room (" + UNLIMITED_MAX_CHARS + " for unlimited)")
	private int maxChars = 512;

	public boolean isHelp()
	{
		return help;
	}

	public boolean isVerboseOutput()
	{
		return verboseOutput;
	}

	public String getServer()
	{
		return server;
	}

	public String getUsername()
	{
		return username;
	}

	public char[] getPassword()
	{
		return password.toCharArray();
	}

	public String getRoomIdOrAlias()
	{
		return roomIdOrAlias;
	}

	public Path getConfigurationFile()
	{
		if (configurationFilePath == null)
		{
			final String configFileToLoad = configurationFile == null ? DEFAULT_CONFIG_FILE : configurationFile;

			if (configFileToLoad.startsWith("~" + File.separator))
			{
				configurationFilePath = Paths.get(System.getProperty("user.home") + configFileToLoad.substring(1));
			}
			else if (configFileToLoad.startsWith("~"))
			{
				throw new UnsupportedOperationException("Home dir expansion not implemented for explicit usernames");
			}
			else
			{
				configurationFilePath = Paths.get(configFileToLoad);
			}
		}
		return configurationFilePath;
	}

	public int getMaxChars()
	{
		return maxChars;
	}

	public boolean isUnlimitedMaxChars()
	{
		return maxChars == UNLIMITED_MAX_CHARS;
	}
}