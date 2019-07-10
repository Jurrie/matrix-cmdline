package org.jurr.matrix.client.commandline;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import com.beust.jcommander.IDefaultProvider;

public class ConfigFileProvider implements IDefaultProvider
{
	private Properties properties;

	public ConfigFileProvider(final Path configurationFile)
	{
		properties = new Properties();
		try
		{
			if (Files.isReadable(configurationFile))
			{
				try (InputStream newInputStream = Files.newInputStream(configurationFile))
				{
					properties.load(newInputStream);
				}
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDefaultValueFor(final String optionName)
	{
		final String key = optionName.substring(2); // Trim leading --
		return properties.getProperty(key);
	}
}
