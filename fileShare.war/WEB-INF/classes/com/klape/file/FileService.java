package com.klape.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;
import java.net.URLDecoder;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.ws.rs.core.Response;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;

@Stateless
@Path("/")
public class FileService
{
  private Logger log = Logger.getLogger(this.getClass());
  private final static String FILES_PATH = "/Users/klape/uploads";

  private static FileDao dao = new FileDao(FILES_PATH+"/files.xml");

  @GET
  @Path("/{id}")
  public Response download(@PathParam("id") int id)
  {
    FileType file = dao.getById(id);
    if(file == null)
    {
      String json = "{\"success\": false, \"reason\": \"File ID not found\"}";
      return Response.status(404).entity(json).type("application/json").build();
    }
    File realFile = new File(file.getPath());
    FileDataSource fds = new FileDataSource(realFile);
    return Response.ok()
                   .entity(fds)
                   .type(file.getContentType().equals("") ? "*/*" : file.getContentType())
                   .header("Content-Disposition","attachment; filename=\"" + file.getName() + "\"")
                   .build();
  }

  @POST
  @Path("/upload")
  @Produces({"application/json"})
  public FileWrapper upload(DataSource handler, 
    @HeaderParam("X-File-Name") String filename, @HeaderParam("X-Mime-Type") String type)
    throws IOException, FileNotFoundException
  {
    File newFile = null;

    if(handler instanceof FileDataSource)
    {
      //The provider already did all the filesystem work, 
      //so now just move it to the uploads folder.  
      //Using MD5 hash as filename.
      File tempFile = ((FileDataSource)handler).getFile();
      newFile  = new File(FILES_PATH + "/" + tempFile.getName());
      log.info("Rename again: " + newFile.getPath());
      tempFile.renameTo(newFile);
    }
    else
    {
      log.error("Was expecting a FileDataSource, but instead got " + handler.getClass().getName());
      return new FileWrapper().setSuccess("false");
    }
    
    FileType file = new FileType();
    file.setPath(newFile.getAbsolutePath());
    file.setContentType(type);
    file.setName(URLDecoder.decode(filename));
    file.setSize(convertSize(newFile.length()));

    dao.add(file);

    FileWrapper wrapper = new FileWrapper();
    wrapper.setSuccess("true");
    wrapper.setFile(file);

    return wrapper;
  }

  @DELETE
  @Path("/{id}")
  @Produces({"application/json"})
  public String remove(@PathParam("id") int id)
  {
    FileType ft = dao.remove(id);
    if(ft == null)
      throw new IllegalStateException("Failed to remove");
    return "{\"success\": true}";
  }

  @GET
  @Path("/list")
  @Produces({"application/json"})
  public List<FileType> list()
    throws IOException
  {
    return dao.getAll();
  }

  private String convertSize(long length)
  {
    StringBuilder builder = new StringBuilder();
    int exp = 1;
    String suffix = "B";

    if(length > Math.pow(2, 30))
    {
      exp = 30;
      suffix = "G";
    }
    else if(length > Math.pow(2, 20))
    {
      exp = 20;
      suffix = "M";
    }
    else if(length > Math.pow(2, 10))
    {
      exp = 10;
      suffix = "K";
    }

    double f = ((double)length) / Math.pow(2, exp);

    if(suffix.equals("B"))
      return "" + length + suffix;
    else
      return new DecimalFormat("0.0" + suffix).format(f);
  }

  private class FileWrapper
  {
    private String success;
    private FileType file;
    
    public FileWrapper setSuccess(String success)
    {
      this.success = success;
      return this;
    }

    public String getSuccess()
    {
      return success;
    }

    public FileWrapper setFile(FileType file)
    {
      this.file = file;
      return this;
    }

    public FileType getFile()
    {
      return file;
    }
  }
}
