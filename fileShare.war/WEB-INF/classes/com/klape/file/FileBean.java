package com.klape.file;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.ejb.EJB;

import org.jboss.logging.Logger;

@ManagedBean(name="file")
@RequestScoped
public class FileBean
{
  private Logger log = Logger.getLogger(this.getClass());

  @EJB
  private FileService service;

  public List<FileType> getList()
  {
    try
    {
      return service.list();
    }
    catch(IOException ioe)
    {
      return null;
    }
  }
}
