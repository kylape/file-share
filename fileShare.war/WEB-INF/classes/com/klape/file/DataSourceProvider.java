package com.klape.file;

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

@Provider
@Consumes("*/*")
@Produces("*/*")
public class DataSourceProvider extends org.jboss.resteasy.plugins.providers.DataSourceProvider
{
   public static DataSource readDataSource(final InputStream in, final MediaType mediaType) throws IOException
   {
      byte[] memoryBuffer = new byte[4096];
      int readCount = in.read(memoryBuffer, 0, memoryBuffer.length);

      File tempFile = null;
      if (in.available() > 0)
      {
         tempFile = File.createTempFile("resteasy-provider-datasource", null);
         FileOutputStream fos = new FileOutputStream(tempFile);
         try
         {
            if(readCount > 0)
               fos.write(memoryBuffer, 0, readCount);

            ProviderHelper.writeTo(in, fos);
         }
         finally
         {
            fos.close();
         }
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
}
