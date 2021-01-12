package fr.training.samples.spring.shop.importjob;

import fr.training.samples.spring.shop.common.FullReportListener;
import fr.training.samples.spring.shop.domain.item.Item;
import fr.training.samples.spring.shop.domain.item.ItemRepository;
import fr.training.samples.spring.shop.exportjob.CustomerDto;
import fr.training.samples.spring.shop.exportjob.ExportCustomerJobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;

@Configuration
public class ImportItemJobConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportItemJobConfig.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Autowired
    private FullReportListener listener;

    @Autowired
    private DeleteTasklet deleteTasklet;

    @Autowired
    private ItemRepository itemRepository;

    @Bean
    public Step deleteStep() {
        return stepBuilderFactory.get("delete-step") //
                .tasklet(deleteTasklet) //
                .build();
    }



//    @Bean
//    public Step importStep() {
//        return stepBuilderFactory.get("import-step").<ItemDto, ItemDto>chunk(10) //
//                .reader(importReader(null)) //
//                .processor(importProcessor()) //
//                .writer(importWriter()) //
//                .faultTolerant() // 3 lignes pour lui dire de continuer en cas d'erreur dans le fichier source 1/3
//                .skipPolicy(new CustomSkipPolicy()) // autre méthode de skip, plus besoin des 2 lignes suivantes
//                // .skip(FlatFileParseException.class) // 2/3
//                //.skipLimit(2) // limit acceptée de nb d'erreur 3/3.
//                .build();
//    }

    @Bean
    public Step importStep() {
        return stepBuilderFactory.get("import-step") //
                .<ItemDto, Item>chunk(5) //
                .reader(importReader(null)) //
                .processor(importProcessor()) //
                .writer(importWriter()) ///
                .build();
    }

    @Bean(name = "importJob")
    public Job importJob() {
        return jobBuilderFactory.get("import-job") //
                .incrementer(new RunIdIncrementer()) //
                .start(deleteStep())
                .next(importStep())
                .listener(listener) // affiche un rapport complet en fin de lancement
                .build();
    }

//    Batch DDD - nouvelle version plus bas
//    @Bean
//    public JdbcBatchItemWriter<ItemDto> importWriter() {
//        final JdbcBatchItemWriter<ItemDto> writer = new JdbcBatchItemWriter<ItemDto>();
//        writer.setDataSource(dataSource);
//        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<ItemDto>());
//        writer.setSql("INSERT INTO item(id, description, price, version) VALUES (:id, :description, :price, 1)");
//        return writer;
//    }

    @Bean
    public ItemWriterAdapter<Item> importWriter() {
        final ItemWriterAdapter<Item> writer = new ItemWriterAdapter<Item>();
        writer.setTargetObject(itemRepository);
        writer.setTargetMethod("save");
        return writer;
    }

    //Batch DDD - remplacé par nouvelle version
//    @Bean
//    public ItemProcessor<ItemDto, ItemDto> importProcessor() {
//        return new ItemProcessor<ItemDto, ItemDto>() {
//
//            @Override
//            public ItemDto process(final ItemDto item) throws Exception {
//                LOGGER.info("Processing {}", item);
//                return item;
//            }
//        };
//    }

    private ItemProcessor<ItemDto, Item> importProcessor() {
        return new ItemProcessor<ItemDto, Item>() {

            @Override
            public Item process(final ItemDto itemDto) throws Exception {
                final Item item = Item.builder().description(itemDto.getDescription()).price(itemDto.getPrice())
                        .build();
                LOGGER.info(item.toString());
                return item;
            }
        };
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    public FlatFileItemReader<ItemDto> importReader(@Value("#{jobParameters['input-file']}") final String inputFile) {
        final FlatFileItemReader<ItemDto> reader = new FlatFileItemReader<ItemDto>();
        final DefaultLineMapper<ItemDto> lineMapper = new DefaultLineMapper<ItemDto>();

        final DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(";");
        tokenizer.setNames(new String[] { "id", "description", "price" });
        lineMapper.setLineTokenizer(tokenizer);

        final BeanWrapperFieldSetMapper<ItemDto> fieldSetMapper = new BeanWrapperFieldSetMapper<ItemDto>();
        fieldSetMapper.setTargetType(ItemDto.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setResource(new FileSystemResource(inputFile));
        reader.setLineMapper(lineMapper);
        reader.setLinesToSkip(1); //on passe la 1ère ligne
        return reader;
    }


}
