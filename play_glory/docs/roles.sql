insert into lg_role( role_id, name ) values ( 100, 'DepositanteE' );
insert into lg_role( role_id, name ) values ( 200, 'DepositanteES' );
insert into lg_role( role_id, name ) values ( 300, 'Supervisor' );
insert into lg_role( role_id, name ) values ( 400, 'Soporte' );


delete from lg_acl_rule where role_id = 100;
insert into lg_acl_rule( acl_id, role_id, operation, permission, priority, resource_id ) 
    select nextval('lg_acl_rule_sequence'), 100, operation, permission, priority, resource_id
    from lg_acl_rule 
    where role_id in 
        ( select role_id 
          from lg_role 
            where name = 'Menu_mainMenu' 
                    or name = 'BillDepositController' 
                    or name = 'CounterController'
        );


delete from lg_acl_rule where role_id = 200;
insert into lg_acl_rule( acl_id, role_id, operation, permission, priority, resource_id ) 
    select nextval('lg_acl_rule_sequence'), 200, operation, permission, priority, resource_id
    from lg_acl_rule 
    where role_id in 
        ( select role_id 
          from lg_role 
            where      name = 'Menu_mainMenu' 
                    or name = 'BillDepositController' 
                    or name = 'EnvelopeDepositController'
                    or name = 'CounterController'
       );


delete from lg_acl_rule where role_id = 300;
insert into lg_acl_rule( acl_id, role_id, operation, permission, priority, resource_id ) 
    select nextval('lg_acl_rule_sequence'), 300, operation, permission, priority, resource_id
    from lg_acl_rule 
    where role_id in 
        ( select role_id 
          from lg_role 
            where      name = 'Menu_mainMenu' 
                    or name = 'BillDepositController' 
                    or name = 'EnvelopeDepositController'
                    or name = 'Menu_otherMenu'
                    or name = 'Menu_accountingMenu'
                    or name = 'Menu_reportMenu'
                    or name = 'CounterController'
        );
insert into lg_acl_rule( acl_id, role_id, operation, permission, priority, resource_id ) 
    select nextval('lg_acl_rule_sequence'), 300, operation, permission, priority, resource_id
    from lg_acl_rule 
    where resource_id in 
        ( select resource_id
          from lg_resource 
            where      name = 'ReportZController.print' 
                    or name = 'ReportZController.list' 
                    or name = 'ReportZController.detail' 
                    or name = 'ReportZController.rotateZ'
                    or name = 'ReportZController.reprint'
                    or name = 'ReportDepositController.reprint'
                    or name = 'ReportDepositController.list'
                    or name = 'ReportDepositController.detail'
        );




delete from lg_acl_rule where role_id = 400;
insert into lg_acl_rule( acl_id, role_id, operation, permission, priority, resource_id ) 
    select nextval('lg_acl_rule_sequence'), 400, operation, permission, priority, resource_id
    from lg_acl_rule 
    where role_id in 
        ( select role_id 
          from lg_role 
            where      name = 'Menu_mainMenu' 
                    or name = 'Menu_hardwareMenu'
                    or name = 'Menu_printTemplateMenu'
                    or name = 'Menu_otherMenu'
                    or name = 'CounterController'
                    or name = 'GloryController'
                    or name = 'IoBoardController'
                    or name = 'PrinterController'
        );
insert into lg_acl_rule( acl_id, role_id, operation, permission, priority, resource_id ) 
    select nextval('lg_acl_rule_sequence'), 400, operation, permission, priority, resource_id
    from lg_acl_rule 
    where resource_id in 
        ( select resource_id
          from lg_resource 
            where      name = 'Application.reset' 
                    or name = 'Application.storingReset' 
        );

