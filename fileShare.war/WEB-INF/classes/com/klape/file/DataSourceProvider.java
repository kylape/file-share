package com.klape.file;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.jboss.resteasy.plugins.providers.ProviderHelper;

import org.jboss.logging.Logger;

@Provider
@Consumes("*/*")
@Produces("*/*")
public class DataSourceProvider extends org.jboss.resteasy.plugins.providers.DataSourceProvider
{
  private static Logger log = Logger.getLogger(DataSourceProvider.class);

  public static DataSource readDataSource(final InputStream in, final MediaType mediaType) throws IOException
  {
    byte[] memoryBuffer = new byte[4096];
    int readCount = in.read(memoryBuffer, 0, memoryBuffer.length);

    File tempFile = File.createTempFile("fileshare-provider-datasource", null);
    if(readCount > 0)
    {
      MessageDigest digest = null;
      try
      {
        digest = MessageDigest.getInstance("MD5");
      }
      catch(NoSuchAlgorithmException e)
      {
        throw new IOException("MD5 algorithm not found to calculate file name");
      }

      FileOutputStream fos = new FileOutputStream(tempFile);
      try
      {
        digest.update(memoryBuffer);
        fos.write(memoryBuffer, 0, readCount);

        int i=0;
        byte[] data = new byte[2048];
        while((i = in.read(data)) != -1)
        {
          digest.update((byte)i);
          fos.write(data, 0, i);
        }
      }
      finally
      {
        fos.close();
      }

      //Use the file hash as the name to ensure a unique name
      String path = tempFile.getPath();
      int index = path.lastIndexOf("/");
      String filename = toHexString(digest.digest());
      String filePath = path.substring(0, index + 1);
      log.info("Rename to: " + filePath + filename);
      File digestFile = new File(filePath + filename);
      boolean success = tempFile.renameTo(digestFile);

      if(success)
        return new FileDataSource(digestFile);
      else
        throw new IOException("Failed to rename file to hex name");
    }

    return new FileDataSource(tempFile);
  }

  @Override
  public DataSource readFrom(Class<DataSource> type,
                             Type genericType,
                             Annotation[] annotations,
                             MediaType mediaType,
                             MultivaluedMap<String, String> httpHeaders,
                             InputStream entityStream) throws IOException
  {

    return readDataSource(entityStream, mediaType);
  }

  private static String toHexString(byte[] hash)
  {
    BigInteger bi = new BigInteger(1, hash);
    return String.format("%0" + (hash.length << 1) + "x", bi);
  }
}
