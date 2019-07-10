package org.jurr.matrix.client.commandline;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import io.github.ma1uta.matrix.impl.exception.MatrixException;

public class Main
{
	private static final int EXIT_OK = 0;
	private static final int EXIT_CMDLINE_INVALID = 1;
	private static final int EXIT_EXCEPTION = 2;

	public static void main(String... args) throws InterruptedException
	{
		final ConfigFileProvider cfp = constructConfigFileProvider(args);

		final Settings settings = new Settings();
		final JCommander jCommander = JCommander.newBuilder().addObject(settings).build();
		try
		{
			jCommander.setProgramName(getCurrentExecutable());
			jCommander.setDefaultProvider(cfp);
			jCommander.parse(args);
		}
		catch (ParameterException e)
		{
			System.err.println(e.getLocalizedMessage());
			e.usage();
			System.exit(EXIT_CMDLINE_INVALID);
		}

		if (settings.isHelp())
		{
			jCommander.usage();
			System.exit(EXIT_OK);
		}

		try
		{
			new MatrixClient(settings).start();
		}
		catch (MatrixException | IOException e)
		{
			System.err.println("Error: " + e.getLocalizedMessage());
			System.exit(EXIT_EXCEPTION);
		}
	}

	private static ConfigFileProvider constructConfigFileProvider(final String... args)
	{
		final Settings tmpSettings = new Settings();
		final JCommander tmpJCommander = JCommander.newBuilder().addObject(tmpSettings).build();
		tmpJCommander.parseWithoutValidation(args);
		final ConfigFileProvider cfp = new ConfigFileProvider(tmpSettings.getConfigurationFile());
		return cfp;
	}

	private static String getCurrentExecutable()
	{
		try
		{
			final String uri = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
			final String innerURI = uri.replaceFirst("jar:", "").replaceFirst("file:", "");
			final Path currentExecutablePath = Paths.get(innerURI);
			if (!currentExecutablePath.toFile().isDirectory())
			{
				final Path fileName = currentExecutablePath.getFileName();
				if (fileName != null)
				{
					return fileName.toString();
				}
			}
		}
		catch (final URISyntaxException | RuntimeException e)
		{
			// Do nothing, just return the default
		}

		// We can not determine the jar file from which we run.
		// We are probably running the class directly from within an IDE.
		// Default to returning the canonical name of this class.
		return Main.class.getCanonicalName();
	}
}
