-- app.project_details definition

-- Drop table

-- DROP TABLE app.project_details;

CREATE TABLE app.project_details (
    project_id serial4 NOT NULL,
    project_name varchar(255) NOT NULL,
    project_objective text NULL,
    end_date date NULL,
    no_of_weeks varchar(100) NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    created_by varchar(100) NULL,
    is_active bool DEFAULT true NULL,
    modified_at timestamp NULL,
    modified_by varchar(100) NULL,
    status varchar(50) NULL,
    stage varchar(50) NULL,
    message text NULL,
    client_id int4 NULL,
    customer_id int4 NULL,
    modeling_measure varchar(255) NULL,
    is_trade_calculated bool DEFAULT false NULL,
    created_user_id varchar(100) NULL,
    modified_user_id varchar(100) NULL,
    "uuid" uuid DEFAULT gen_random_uuid() NULL,
    display_name varchar(254) DEFAULT ''::character varying NULL,
    version_id int4 DEFAULT 1 NULL,
    is_active_version bool DEFAULT true NULL,
    project_period varchar(255) NULL,
    is_from_base_version bool DEFAULT false NULL,
    start_date date NULL,
    CONSTRAINT project_details_new_pkey PRIMARY KEY (project_id)
);

INSERT INTO app.file_upload (id,project_id,original_file_name,mime_type,file_size_bytes,bytes_received,upload_etag,checksum_sha256,checksum_source_sha256,upload_status,csv_path,monet_table_name,row_count,ingest_status,ingested_at,created_at,created_user_id,created_by,modified_at,modified_user_id,modified_by,driver_details_id) VALUES
     (81,922,'Liquid Mix - UAT - Natures Bounty -Templated CLEAN 10JULY_CPG1.xlsx',NULL,11449704,11449704,'9eb957621ef84dccbb0dc28024e3c968','85a061f0c12e15285fdb24fca0b233ea6af7a1d88fb19b1d7c02f3d7462d28e8','675288c8e0af429607a9311f2d065cf190f18d21772d069da08802c887df4f6e','COMPLETED','C:\Code\mix_file_upload\922\81\Liquid Mix - UAT - Natures Bounty -Templated CLEAN 10JULY_CPG1.csv','stg_p922_f81',125004,'INGESTED','2025-11-16 17:42:35.086','2025-11-16 17:41:33.574','hbhasu01zcs','Sri Harsha Bhasuru','2025-11-16 17:42:20.276','hbhasu01zcs','Sri Harsha Bhasuru',51);


INSERT INTO app.project_details (project_id,project_name,project_objective,end_date,no_of_weeks,created_at,created_by,is_active,modified_at,modified_by,status,stage,message,client_id,customer_id,modeling_measure,is_trade_calculated,created_user_id,modified_user_id,"uuid",display_name,version_id,is_active_version,project_period,is_from_base_version,start_date) VALUES
     (922,'Custom_VolumeEQ_IsFromUIExtraction_Test2',NULL,'2025-10-12','104','2025-11-10 23:29:03.571','Sharmitha Sakthivel',true,'2025-11-11 22:41:25.824','Rijo Mon','In-Progress','checklistAndLoadStatus',NULL,2,3301,'VOLUME',NULL,'ssakth01zcs','prrmozcs','77a0eafd-d93f-4794-a566-97a60fa5a15d'::uuid,'Custom_VolumeEQ_IsFromUIExtraction_Test2',1,true,'104',false,'2023-10-22');

INSERT INTO app.source_column (file_upload_id,ordinal,name_original,name_sanitized,data_type,mapping_status,created_at,created_user_id,created_by,modified_at,modified_user_id,modified_by) VALUES
     (81,1,'Subbrand','subbrand','TEXT','UNMAPPED','2025-11-16 17:42:20.279',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,2,'Project (Output) Geography','project_output_geography','TEXT','UNMAPPED','2025-11-16 17:42:20.379',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,3,'Input Geography','input_geography','TEXT','UNMAPPED','2025-11-16 17:42:20.492',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,4,'Input Geography Granularity','input_geography_granularity','TEXT','UNMAPPED','2025-11-16 17:42:20.586',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,5,'Week ending Sunday','week_ending_sunday','DATE','UNMAPPED','2025-11-16 17:42:20.673',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,6,'Marketing Driver','marketing_driver','TEXT','UNMAPPED','2025-11-16 17:42:20.765',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,7,'Custom Driver Name','custom_driver_name','TEXT','UNMAPPED','2025-11-16 17:42:20.939',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,8,'Campaign','campaign','TEXT','UNMAPPED','2025-11-16 17:42:21.128',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,9,'Primary Model Variable Name','primary_model_variable_name','TEXT','UNMAPPED','2025-11-16 17:42:21.311',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,10,'Creative','creative','TEXT','UNMAPPED','2025-11-16 17:42:21.492',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL);

INSERT INTO app.source_column (file_upload_id,ordinal,name_original,name_sanitized,data_type,mapping_status,created_at,created_user_id,created_by,modified_at,modified_user_id,modified_by) VALUES
     (81,11,'Daypart','daypart','TEXT','UNMAPPED','2025-11-16 17:42:21.676',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,12,'Duration','duration','TEXT','UNMAPPED','2025-11-16 17:42:21.858',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,13,'Network / Site / Platform','network_site_platform','TEXT','UNMAPPED','2025-11-16 17:42:22.037',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,14,'Partner','partner','TEXT','UNMAPPED','2025-11-16 17:42:22.229',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,15,'Search Branded / NonBranded / Competitor','search_branded_nonbranded_competitor','TEXT','UNMAPPED','2025-11-16 17:42:22.411',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,16,'Search Keyword Group','search_keyword_group','TEXT','UNMAPPED','2025-11-16 17:42:22.596',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,17,'Targeting Type','targeting_type','TEXT','UNMAPPED','2025-11-16 17:42:22.784',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,18,'Vehicle or Ad Type','vehicle_or_ad_type','TEXT','UNMAPPED','2025-11-16 17:42:22.979',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,19,'Support Metric','support_metric','TEXT','UNMAPPED','2025-11-16 17:42:23.168',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL),
     (81,20,'Support','support','DECIMAL','UNMAPPED','2025-11-16 17:42:23.356',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL);

INSERT INTO app.source_column (file_upload_id,ordinal,name_original,name_sanitized,data_type,mapping_status,created_at,created_user_id,created_by,modified_at,modified_user_id,modified_by) VALUES
     (81,21,'Spend (Cost of Activity)','spend_cost_of_activity','DECIMAL','UNMAPPED','2025-11-16 17:42:23.556',NULL,'Sri Harsha Bhasuru',NULL,NULL,NULL);

INSERT INTO app.file_mapping_status (file_upload_id,total_required_count,required_mapped_count,total_mapped_count,is_marked_as_mapped,created_at,created_user_id,created_by,modified_at,modified_user_id,modified_by) VALUES
     (81,9,0,0,false,'2025-11-16 17:42:34.955','hbhasu01zcs','Sri Harsha Bhasuru','2025-11-16 17:42:34.955','hbhasu01zcs','Sri Harsha Bhasuru');
INSERT INTO meta.driver_details (id,driver_type_id,driver_name,is_active,status,custom_name,is_hidden,relevance_order) VALUES
     (51,4,'MultiFileUpload',true,'Not Started','',false,52);
INSERT INTO meta.driver_iterative_column_map (driver_id,column_name,column_datatype,time_format,is_mandatory,is_active,column_value,sort_order,tooltip) VALUES
     (51,'Marketing Driver','TEXT',NULL,false,true,NULL,6,'Classification of marketing channel, under which separate campaigns or secondary model tactics reside. We require mapping to Circana''s Driver taxonomy so that Liquid Mix can leverage benchmarks and domain expertise to meet model expectations. You can still use a custom driver name (next column).'),
     (51,'Custom Driver Name','TEXT',NULL,false,true,NULL,7,'Customized name of your choosing to replace the Marketing Driver''s standard name. Note that you can give one Custom Driver Name for an entire Marketing Driver or split a Marketing Driver up into 2 or more groupings of campaigns and tactics.'),
     (51,'Spend (Cost of Activity)','DECIMAL',NULL,true,true,NULL,21,'Spend is the cost of marketing activity. Include numbers only, include the working costs of your marketing activities.'),
     (51,'Duration','TEXT','MM/dd/yyyy',false,true,NULL,12,'Secondary Model tactics breakdown Driver-level results from the Primary model into up to 9 different types of tactics. Used mainly in TV and Video, Duration defines the ad length of time.'),
     (51,'Primary Model Variable Name','TEXT',NULL,false,true,NULL,9,'Text name which will represent the Campaign. We recommend a concatenation of "SubBrand_Driver_Campaign"'),
     (51,'Creative','TEXT',NULL,false,true,NULL,10,'Secondary Model tactics breakdown Driver-level results from the Primary model into up to 9 different types of tactics. Creative references the visual and textual aspects of marketing that influence consumers to buy.'),
     (51,'Daypart','TEXT',NULL,false,true,NULL,11,'Secondary Model tactics breakdown Driver-level results from the Primary model into up to 9 different types of tactics. Used in TV drivers, Dayparts are standardized times and weekday/weekend splits.'),
     (51,'Network / Site / Platform','TEXT',NULL,false,true,NULL,13,'Secondary Model tactics breakdown Driver-level results from the Primary model into up to 9 different types of tactics.  Used mainly in Paid Search, Digital Display, Streaming Audio, Online Video, and TV, this type identifies the ad placement by marketing provider.'),
     (51,'Partner','TEXT',NULL,false,true,NULL,14,'Secondary Model tactics breakdown Driver-level results from the Primary model into up to 9 different types of tactics. This type identifies the ad technology partner.'),
     (51,'Search Branded / NonBranded / Competitor','TEXT',NULL,false,true,NULL,15,'Secondary Model tactics breakdown Driver-level results from the Primary model into up to 9 different types of tactics. Used in Search, this type is a standard classification of search strategy.');
INSERT INTO meta.driver_iterative_column_map (driver_id,column_name,column_datatype,time_format,is_mandatory,is_active,column_value,sort_order,tooltip) VALUES
     (51,'Search Keyword Group','TEXT',NULL,false,true,NULL,16,'Secondary Model tactics breakdown Driver-level results from the Primary model into up to 9 different types of tactics. Used in Search, this type is a customed group name for your keywords.'),
     (51,'Targeting Type','TEXT',NULL,false,true,NULL,17,'Secondary Model tactics breakdown Driver-level results from the Primary model into up to 9 different types of tactics. Used mainly in Digital Display and Streaming TV, Targeting Type classifies the methodology used in targeting.'),
     (51,'Vehicle or Ad Type','TEXT',NULL,false,true,NULL,18,'Secondary Model tactics breakdown Driver-level results from the Primary model into up to 9 different types of tactics. Usually used in PR and Influencers, this type identifies the media channel used.'),
     (51,'Sub-Brand','TEXT',NULL,true,true,NULL,1,'Product Group to be modeled, per configuration. Permitted names are a picklist within the template. Aggregations to Brands and linkages to Competitors will be automatic. For the Marketing Campaign in this row, if the target of the creative applies to only 1 Subbrand, enter that Sub-brand name here. Instead, if the creative is common for all sub-brands (i.e. Masterbrand campaign), enter "Shared - <Brand>" (i.e. "Shared - Nordic Yogurt"). Do not create repeat rows for multiple Sub-Brands.'),
     (51,'Project (Output) Geography','TEXT',NULL,true,true,NULL,2,'Geography to be modeled and reported separately, per configuration. Permitted names are a picklist within the template. Aggregations with any other reporting geographies will be automatic. For the Marketing Campaign in this row, if the target ofthe creative applies to only 1 reporting geography (i.e. Retailer Shopper Marketing is not applicable to an Amazon Sales model), enter that specific name here (i.e. "Total US - Multi Outlet"). Instead, if the creative is common for all reporting geographies, enter "Shared for all Geographies". Do not create repeat rows for multiple Reporting Geographies.'),
     (51,'Input Geography','TEXT',NULL,true,true,NULL,3,'Geography granularity of the marketing inputs. For instance, if TV or Social data is available at DMA level, you will provide the DMA names here.'),
     (51,'Input Geography Granularity','TEXT',NULL,true,true,NULL,4,'Type of geography of the marketing inputs. For instance, if TV or Social data is available at DMA level, you will enter the select the term "DMA" from the picklist.'),
     (51,'Week Ending Sunday','DATE','MM/dd/yyyy',true,true,NULL,5,'Week ending dates for your marketing data should end on a Sunday. If you use a different Day of Week, Circana will convert it to Sunday ending. The date format must be DD-MMM-YYYY since standards differ globally, which requires a custom format in Excel.'),
     (51,'Support','DECIMAL',NULL,true,true,NULL,20,'Support is the metric used as the modeling input KPI. Include numbers only, using the full number in support metric shown in the prior column (i.e. do not include in MMs or 000s).'),
     (51,'Campaign','TEXT',NULL,true,true,NULL,8,'Lowest level of Marketing execution and the level at which models are run. Results for higher levels like Driver are built from the Campaign up. Only enter the campaign name for major campaigns that you want measured in the main model. Creative level breakouts are best measured in the "Secondary Model Tactics" section.');
INSERT INTO meta.driver_iterative_column_map (driver_id,column_name,column_datatype,time_format,is_mandatory,is_active,column_value,sort_order,tooltip) VALUES
     (51,'Support Metric','TEXT',NULL,true,true,NULL,19,'Support is the metric used as the modeling input KPI and the Support Metric is the type of support, such as Impressions or GRPs. For consistency of reporting at Driver level, the Support Metric should be consistent for a given Brand and Marketing Driver or Custom Driver Name.');
