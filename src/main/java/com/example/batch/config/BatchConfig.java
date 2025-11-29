package com.example.batch.config;

import com.example.batch.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Configuration
public class BatchConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ExcelWriter excelWriter;

    @Bean
    public JdbcCursorItemReader<Customer> customerReader() {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerReader")
                .dataSource(dataSource)
                .sql("SELECT customer_id, name, email, mobile_number, created_at, created_by, updated_at, updated_by FROM customer")
                .rowMapper(new CustomerRowMapper())
                .build();
    }

    @Bean
    public Step exportCustomerStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
        return new StepBuilder("exportCustomerStep", jobRepository)
                .<Customer, Customer>chunk(100, transactionManager)
                .reader(customerReader())
                .writer(excelWriter)
                .build();
    }

    @Bean
    public Job exportCustomerJob(JobRepository jobRepository, Step exportCustomerStep) {
        return new JobBuilder("exportCustomerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(exportCustomerStep)
                .build();
    }

    private static class CustomerRowMapper implements RowMapper<Customer> {
        @Override
        public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
            Customer customer = new Customer();
            customer.setCustomerId(rs.getInt("customer_id"));
            customer.setName(rs.getString("name"));
            customer.setEmail(rs.getString("email"));
            customer.setMobileNumber(rs.getString("mobile_number"));

            java.sql.Date createdAt = rs.getDate("created_at");
            if (createdAt != null) {
                customer.setCreatedAt(createdAt.toLocalDate());
            }

            customer.setCreatedBy(rs.getString("created_by"));

            java.sql.Date updatedAt = rs.getDate("updated_at");
            if (updatedAt != null) {
                customer.setUpdatedAt(updatedAt.toLocalDate());
            }

            customer.setUpdatedBy(rs.getString("updated_by"));

            return customer;
        }
    }
}
