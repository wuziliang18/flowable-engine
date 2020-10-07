

CREATE TABLE [ACT_DMN_DATABASECHANGELOGLOCK] ([ID] [int] NOT NULL, [LOCKED] [bit] NOT NULL, [LOCKGRANTED] [datetime2](3), [LOCKEDBY] [nvarchar](255), CONSTRAINT [PK_ACT_DMN_DATABASECHANGELOGLOCK] PRIMARY KEY ([ID]))

DELETE FROM [ACT_DMN_DATABASECHANGELOGLOCK]

INSERT INTO [ACT_DMN_DATABASECHANGELOGLOCK] ([ID], [LOCKED]) VALUES (1, 0)

UPDATE [ACT_DMN_DATABASECHANGELOGLOCK] SET [LOCKED] = 1, [LOCKEDBY] = '192.168.10.1 (192.168.10.1)', [LOCKGRANTED] = '2020-10-06T16:17:05.639' WHERE [ID] = 1 AND [LOCKED] = 0

CREATE TABLE [ACT_DMN_DATABASECHANGELOG] ([ID] [nvarchar](255) NOT NULL, [AUTHOR] [nvarchar](255) NOT NULL, [FILENAME] [nvarchar](255) NOT NULL, [DATEEXECUTED] [datetime2](3) NOT NULL, [ORDEREXECUTED] [int] NOT NULL, [EXECTYPE] [nvarchar](10) NOT NULL, [MD5SUM] [nvarchar](35), [DESCRIPTION] [nvarchar](255), [COMMENTS] [nvarchar](255), [TAG] [nvarchar](255), [LIQUIBASE] [nvarchar](20), [CONTEXTS] [nvarchar](255), [LABELS] [nvarchar](255), [DEPLOYMENT_ID] [nvarchar](10))

CREATE TABLE [ACT_DMN_DEPLOYMENT] ([ID_] [varchar](255) NOT NULL, [NAME_] [varchar](255), [CATEGORY_] [varchar](255), [DEPLOY_TIME_] [datetime], [TENANT_ID_] [varchar](255), [PARENT_DEPLOYMENT_ID_] [varchar](255), CONSTRAINT [PK_ACT_DMN_DEPLOYMENT] PRIMARY KEY ([ID_]))

CREATE TABLE [ACT_DMN_DEPLOYMENT_RESOURCE] ([ID_] [varchar](255) NOT NULL, [NAME_] [varchar](255), [DEPLOYMENT_ID_] [varchar](255), [RESOURCE_BYTES_] [varbinary](MAX), CONSTRAINT [PK_ACT_DMN_DEPLOYMENT_RESOURCE] PRIMARY KEY ([ID_]))

CREATE TABLE [ACT_DMN_DECISION_TABLE] ([ID_] [varchar](255) NOT NULL, [NAME_] [varchar](255), [VERSION_] [int], [KEY_] [varchar](255), [CATEGORY_] [varchar](255), [DEPLOYMENT_ID_] [varchar](255), [PARENT_DEPLOYMENT_ID_] [varchar](255), [TENANT_ID_] [varchar](255), [RESOURCE_NAME_] [varchar](255), [DESCRIPTION_] [varchar](255), CONSTRAINT [PK_ACT_DMN_DECISION_TABLE] PRIMARY KEY ([ID_]))

INSERT INTO [ACT_DMN_DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [ORDEREXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [EXECTYPE], [CONTEXTS], [LABELS], [LIQUIBASE], [DEPLOYMENT_ID]) VALUES ('1', 'activiti', 'org/flowable/dmn/db/liquibase/flowable-dmn-db-changelog.xml', GETDATE(), 1, '7:d878c2672ead57b5801578fd39c423af', 'createTable tableName=ACT_DMN_DEPLOYMENT; createTable tableName=ACT_DMN_DEPLOYMENT_RESOURCE; createTable tableName=ACT_DMN_DECISION_TABLE', '', 'EXECUTED', NULL, NULL, '3.5.3', '1993825802')

CREATE TABLE [ACT_DMN_HI_DECISION_EXECUTION] ([ID_] [varchar](255) NOT NULL, [DECISION_DEFINITION_ID_] [varchar](255), [DEPLOYMENT_ID_] [varchar](255), [START_TIME_] [datetime], [END_TIME_] [datetime], [INSTANCE_ID_] [varchar](255), [EXECUTION_ID_] [varchar](255), [ACTIVITY_ID_] [varchar](255), [FAILED_] [bit] CONSTRAINT [DF_ACT_DMN_HI_DECISION_EXECUTION_FAILED_] DEFAULT 0, [TENANT_ID_] [varchar](255), [EXECUTION_JSON_] [varchar](MAX), CONSTRAINT [PK_ACT_DMN_HI_DECISION_EXECUTION] PRIMARY KEY ([ID_]))

INSERT INTO [ACT_DMN_DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [ORDEREXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [EXECTYPE], [CONTEXTS], [LABELS], [LIQUIBASE], [DEPLOYMENT_ID]) VALUES ('2', 'flowable', 'org/flowable/dmn/db/liquibase/flowable-dmn-db-changelog.xml', GETDATE(), 2, '7:15a6bda1fce898a58e04fe6ac2d89f54', 'createTable tableName=ACT_DMN_HI_DECISION_EXECUTION', '', 'EXECUTED', NULL, NULL, '3.5.3', '1993825802')

ALTER TABLE [ACT_DMN_HI_DECISION_EXECUTION] ADD [SCOPE_TYPE_] [varchar](255)

INSERT INTO [ACT_DMN_DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [ORDEREXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [EXECTYPE], [CONTEXTS], [LABELS], [LIQUIBASE], [DEPLOYMENT_ID]) VALUES ('3', 'flowable', 'org/flowable/dmn/db/liquibase/flowable-dmn-db-changelog.xml', GETDATE(), 3, '7:eed5dec2f94778b62d0b0b4beebc191d', 'addColumn tableName=ACT_DMN_HI_DECISION_EXECUTION', '', 'EXECUTED', NULL, NULL, '3.5.3', '1993825802')

ALTER TABLE [ACT_DMN_DECISION_TABLE] DROP COLUMN [PARENT_DEPLOYMENT_ID_]

INSERT INTO [ACT_DMN_DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [ORDEREXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [EXECTYPE], [CONTEXTS], [LABELS], [LIQUIBASE], [DEPLOYMENT_ID]) VALUES ('4', 'flowable', 'org/flowable/dmn/db/liquibase/flowable-dmn-db-changelog.xml', GETDATE(), 4, '7:b8d3d5a3efb71aef7578e1130a38fde2', 'dropColumn columnName=PARENT_DEPLOYMENT_ID_, tableName=ACT_DMN_DECISION_TABLE', '', 'EXECUTED', NULL, NULL, '3.5.3', '1993825802')

CREATE UNIQUE NONCLUSTERED INDEX ACT_IDX_DEC_TBL_UNIQ ON [ACT_DMN_DECISION_TABLE]([KEY_], [VERSION_], [TENANT_ID_])

INSERT INTO [ACT_DMN_DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [ORDEREXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [EXECTYPE], [CONTEXTS], [LABELS], [LIQUIBASE], [DEPLOYMENT_ID]) VALUES ('6', 'flowable', 'org/flowable/dmn/db/liquibase/flowable-dmn-db-changelog.xml', GETDATE(), 5, '7:c44cb06af8977c776a4e93aebe96c568', 'createIndex indexName=ACT_IDX_DEC_TBL_UNIQ, tableName=ACT_DMN_DECISION_TABLE', '', 'EXECUTED', NULL, NULL, '3.5.3', '1993825802')

DROP INDEX ACT_IDX_DEC_TBL_UNIQ ON [ACT_DMN_DECISION_TABLE]

exec sp_rename '[ACT_DMN_DECISION_TABLE]', 'ACT_DMN_DECISION'

CREATE UNIQUE NONCLUSTERED INDEX ACT_IDX_DMN_DEC_UNIQ ON [ACT_DMN_DECISION]([KEY_], [VERSION_], [TENANT_ID_])

INSERT INTO [ACT_DMN_DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [ORDEREXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [EXECTYPE], [CONTEXTS], [LABELS], [LIQUIBASE], [DEPLOYMENT_ID]) VALUES ('7', 'flowable', 'org/flowable/dmn/db/liquibase/flowable-dmn-db-changelog.xml', GETDATE(), 6, '7:4b6469565b1b00b428ffca7eab1ef253', 'dropIndex indexName=ACT_IDX_DEC_TBL_UNIQ, tableName=ACT_DMN_DECISION_TABLE; renameTable newTableName=ACT_DMN_DECISION, oldTableName=ACT_DMN_DECISION_TABLE; createIndex indexName=ACT_IDX_DMN_DEC_UNIQ, tableName=ACT_DMN_DECISION', '', 'EXECUTED', NULL, NULL, '3.5.3', '1993825802')

ALTER TABLE [ACT_DMN_DECISION] ADD [DECISION_TYPE_] [varchar](255)

INSERT INTO [ACT_DMN_DATABASECHANGELOG] ([ID], [AUTHOR], [FILENAME], [DATEEXECUTED], [ORDEREXECUTED], [MD5SUM], [DESCRIPTION], [COMMENTS], [EXECTYPE], [CONTEXTS], [LABELS], [LIQUIBASE], [DEPLOYMENT_ID]) VALUES ('8', 'flowable', 'org/flowable/dmn/db/liquibase/flowable-dmn-db-changelog.xml', GETDATE(), 7, '7:f83b7b3228be2c4bbb554d6de45307d7', 'addColumn tableName=ACT_DMN_DECISION', '', 'EXECUTED', NULL, NULL, '3.5.3', '1993825802')

UPDATE [ACT_DMN_DATABASECHANGELOGLOCK] SET [LOCKED] = 0, [LOCKEDBY] = NULL, [LOCKGRANTED] = NULL WHERE [ID] = 1

