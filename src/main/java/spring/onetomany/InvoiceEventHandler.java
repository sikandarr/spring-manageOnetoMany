package spring.onetomany;

import javax.persistence.EntityManager;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.apache.log4j.Logger;

@Component
@RepositoryEventHandler(Invoice.class)
@Transactional
public class InvoiceEventHandler
{
	static Logger log = Logger.getLogger(InvoiceEventHandler.class.getName());

	@Autowired
	EntityManager em;

	@HandleAfterCreate
	public void handleAfterCreate(Invoice i)
	{
		ManageOneToMany.addChildren(i, i.getLineItems());
	}

	@HandleBeforeSave
	public void handleBeforeSave(Invoice i)
	{
		ManageOneToMany.addChildren(i, i.getLineItems());
	}

	@HandleAfterSave
	public void handleAfterSave(Invoice i)
	{
		ManageOneToMany.syncChildren(i, i.getLineItems());
	}

}
