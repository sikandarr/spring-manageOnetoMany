package spring.onetomany;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface LineItemRepository extends PagingAndSortingRepository<LineItem, Integer>
{
	List<LineItem> findByInvoiceId(int id);
}
