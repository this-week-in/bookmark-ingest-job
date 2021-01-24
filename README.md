# The Ingest Service

![CI](https://github.com/the-trump-dump/ingest/workflows/CI/badge.svg)

This ingests all the Pinboard bookmarks related to a particular topic into a SQL database for later use in generating a
roundup.

## Useful Queries

The resulting data is imported into the `bookmark` table. You can query for all the records that have a given `tag` like
so:

```sql
SELECT b.bookmark_id, b.tags
FROM bookmark b
WHERE 'covid' = any (b.tags) 
```