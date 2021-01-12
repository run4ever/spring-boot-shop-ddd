package fr.training.samples.spring.shop.exportjob;

import fr.training.samples.spring.shop.application.customer.CustomerService;
import fr.training.samples.spring.shop.domain.customer.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@StepScope
public class CustomerProcessor implements ItemProcessor<String, CustomerDto> {

    private static final Logger logger = LoggerFactory.getLogger(CustomerProcessor.class);

    @Autowired
    private CustomerService customerService;

    @Override
    public CustomerDto process(final String customerId) throws Exception {
        final Customer customer = customerService.findOne(customerId);
        logger.info("Processing Customer {}", customer);

        final CustomerDto customerDto = new CustomerDto();
        customerDto.setId(customer.getId());
        customerDto.setName(customer.getName());
        customerDto.setPassword(customer.getPassword().getValue());
        customerDto.setEmail(customer.getEmail().getValue());
        customerDto.setStreet(customer.getAddress().getStreet());
        customerDto.setCity(customer.getAddress().getCity());
        customerDto.setCountry(customer.getAddress().getCountry());
        customerDto.setPostalCode(customer.getAddress().getPostalCode());
        return customerDto;
    }
}