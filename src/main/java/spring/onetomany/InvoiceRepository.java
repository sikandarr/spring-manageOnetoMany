package spring.onetomany;

import org.springframework.data.repository.PagingAndSortingRepository;


public interface InvoiceRepository extends PagingAndSortingRepository<Invoice, Integer>
{

}
