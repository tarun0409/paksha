CREATE TABLE items (
                       id UUID PRIMARY KEY,
                       owner_user_id UUID NOT NULL,
                       title VARCHAR(300) NOT NULL,
                       body TEXT NULL,
                       created_at TIMESTAMPTZ NOT NULL,
                       updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE tag_definitions (
                                 id UUID PRIMARY KEY,
                                 owner_user_id UUID NOT NULL,
                                 kind VARCHAR(20) NOT NULL, -- SINGLE/FUNCTIONAL
                                 key VARCHAR(100) NOT NULL,
                                 value_type VARCHAR(20) NULL, -- STRING/NUMBER/BOOL/DATE for FUNCTIONAL
);

CREATE TABLE item_tags (
                           item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
                           tag_definition_id UUID NOT NULL REFERENCES tag_definitions(id) ON DELETE CASCADE,
                           value_string VARCHAR(300) NULL,
                           value_number NUMERIC NULL,
                           value_bool BOOLEAN NULL,
                           value_date DATE NULL,
                           PRIMARY KEY (item_id, tag_definition_id)
);

CREATE INDEX idx_item_tags_tag_item ON item_tags(tag_definition_id, item_id);
CREATE INDEX idx_item_tags_tag_value_string ON item_tags(tag_definition_id, value_string, item_id);
CREATE INDEX idx_item_tags_tag_value_number ON item_tags(tag_definition_id, value_number, item_id);
CREATE INDEX idx_tag_defs_owner_kind_key ON tag_definitions(owner_user_id, kind, key);
CREATE INDEX idx_items_owner_updated ON items(owner_user_id, updated_at DESC);

CREATE TABLE dashboards (
                            id UUID PRIMARY KEY,
                            owner_user_id UUID NOT NULL,
                            name VARCHAR(200) NOT NULL,
                            description TEXT NULL,
                            created_at TIMESTAMPTZ NOT NULL,
                            updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE widgets (
                         id UUID PRIMARY KEY,
                         dashboard_id UUID NOT NULL REFERENCES dashboards(id) ON DELETE CASCADE,
                         title VARCHAR(200) NOT NULL,
                         type VARCHAR(50) NOT NULL,
                         query_spec JSONB NOT NULL,
                         layout JSONB NULL,
                         created_at TIMESTAMPTZ NOT NULL,
                         updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_widgets_dashboard ON widgets(dashboard_id);
