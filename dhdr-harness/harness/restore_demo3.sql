-- Restore demographic 3 to its seeded values (captured 2026-07-23).
UPDATE demographic SET last_name='FAKE-FOREMAN', first_name='FAKE-GEORGE',
  year_of_birth='1985', month_of_birth='04', date_of_birth='05',
  sex='M', hin='1111111165' WHERE demographic_no=3;
