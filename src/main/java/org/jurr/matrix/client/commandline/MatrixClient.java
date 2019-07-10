package org.jurr.matrix.client.commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import io.github.ma1uta.matrix.client.factory.RequestFactory;
import io.github.ma1uta.matrix.client.factory.jaxrs.JaxRsRequestFactory;
import io.github.ma1uta.matrix.client.model.room.JoinRequest;
import io.github.ma1uta.matrix.impl.exception.MatrixException;
import io.github.ma1uta.matrix.support.jackson.JacksonContextResolver;

public final class MatrixClient
{
	private final Settings settings;

	public MatrixClient(final Settings settings)
	{
		this.settings = settings;
	}

	public void start() throws InterruptedException, IOException
	{
		final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		final Client client = ClientBuilder.newBuilder().register(new JacksonContextResolver()).build();
		final RequestFactory requestFactory = new JaxRsRequestFactory(client, settings.getServer(), executorService);

		try (io.github.ma1uta.matrix.client.MatrixClient mxClient = new io.github.ma1uta.matrix.client.MatrixClient(requestFactory))
		{
			login(mxClient, settings.getUsername(), settings.getPassword());

			final String roomId = resolveAlias(mxClient, settings.getRoomIdOrAlias());
			if (!hasJoinedRoom(mxClient, roomId))
			{
				joinRoom(mxClient, roomId);
			}

			sendMessage(mxClient, roomId);

			mxClient.auth().logout();
		}
		finally
		{
			client.close();
			executorService.shutdownNow();
			executorService.awaitTermination(60, TimeUnit.SECONDS);
		}
	}

	private void sendMessage(final io.github.ma1uta.matrix.client.MatrixClient mxClient, final String roomId) throws IOException
	{
		try (InputStreamReader isr = new InputStreamReader(System.in, StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(isr))
		{
			if (settings.isUnlimitedMaxChars())
			{
				sendFullInputStreamAsMessage(mxClient, roomId, br);
			}
			else
			{
				sendPartialInputStreamAsMessage(mxClient, roomId, br);
			}
		}
	}

	private void sendPartialInputStreamAsMessage(final io.github.ma1uta.matrix.client.MatrixClient mxClient, final String roomId, final BufferedReader br) throws IOException
	{
		final char[] cbuf = new char[settings.getMaxChars() + 1];
		int totalNumCharsRead = 0;
		int numCharsRead = 0;
		while (totalNumCharsRead < cbuf.length && numCharsRead != -1)
		{
			numCharsRead = br.read(cbuf, totalNumCharsRead, cbuf.length - totalNumCharsRead);
			totalNumCharsRead += numCharsRead;
		}

		if (totalNumCharsRead <= 0)
		{
			if (settings.isVerboseOutput())
			{
				System.out.println("No message given - nothing sent");
			}
			return;
		}

		final String message;
		if (totalNumCharsRead < cbuf.length)
		{
			message = new String(cbuf);
		}
		else
		{
			message = new String(cbuf, 0, settings.getMaxChars() - 3) + "...";
		}

		mxClient.event().sendMessage(roomId, message);
		if (settings.isVerboseOutput())
		{
			System.out.println("Message sent to room " + roomId);
		}
	}

	private void sendFullInputStreamAsMessage(final io.github.ma1uta.matrix.client.MatrixClient mxClient, final String roomId, final BufferedReader br)
	{
		br.lines().forEach(l -> mxClient.event().sendMessage(roomId, l));

		if (settings.isVerboseOutput())
		{
			System.out.println("End of input stream - full input stream sent to room " + roomId);
		}
	}

	private void login(final io.github.ma1uta.matrix.client.MatrixClient mxClient, final String user, final char[] pass)
	{
		try
		{
			if (settings.isVerboseOutput())
			{
				System.out.println("Attempting login for user " + user + " on host " + settings.getServer());
			}
			mxClient.auth().login(user, pass).join();
			if (settings.isVerboseOutput())
			{
				System.out.println("Login succeeded for user " + user);
			}
		}
		catch (CompletionException e)
		{
			if (e.getCause() instanceof MatrixException)
			{
				throw (MatrixException) e.getCause();
			}
			else
			{
				throw e;
			}
		}
	}

	private String resolveAlias(final io.github.ma1uta.matrix.client.MatrixClient mxClient, final String roomIdOrAlias)
	{
		if (roomIdOrAlias.length() <= 0)
		{
			throw new IllegalArgumentException("Room id or alias should not be empty");
		}

		if (roomIdOrAlias.charAt(0) != '#')
		{
			// Not an alias - probably a room id
			return roomIdOrAlias;
		}

		try
		{
			final String roomId = mxClient.room().resolveAlias(roomIdOrAlias).join().getRoomId();
			if (settings.isVerboseOutput())
			{
				System.out.println("Room alias " + roomIdOrAlias + " resolved to " + roomId);
			}
			return roomId;
		}
		catch (CompletionException e)
		{
			if (e.getCause() instanceof MatrixException)
			{
				throw (MatrixException) e.getCause();
			}
			else
			{
				throw e;
			}
		}
	}

	private boolean hasJoinedRoom(final io.github.ma1uta.matrix.client.MatrixClient mxClient, final String roomId)
	{
		final boolean result = mxClient.room().joinedRooms().join().contains(roomId);
		if (settings.isVerboseOutput())
		{
			System.out.println("User has " + (result ? "already" : "not yet") + " joined room");
		}
		return result;
	}

	private void joinRoom(final io.github.ma1uta.matrix.client.MatrixClient mxClient, final String roomId)
	{
		try
		{
			final JoinRequest request = new JoinRequest();
			mxClient.room().joinById(roomId, request).join();
			if (settings.isVerboseOutput())
			{
				System.out.println("User has joined room now");
			}
		}
		catch (CompletionException e)
		{
			if (e.getCause() instanceof MatrixException)
			{
				throw (MatrixException) e.getCause();
			}
			else
			{
				throw e;
			}
		}
	}
}
