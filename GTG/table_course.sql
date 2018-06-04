create table course
(
  year           int          not null,
  semester       int          not null,
  code           varchar(10)  not null,
  title          varchar(100) not null,
  classification varchar(45)  not null,
  credit         int          not null,
  quota          int          not null,
  time           varchar(100) null,
  instructor     varchar(45)  not null,
  room           varchar(45)  null,
  grade          varchar(100) not null,
  syllabus       varchar(100) null,
  maj_cd         varchar(10)  null,
  cor_cd         varchar(10)  null,
  primary key (year, semester, code),
  constraint course_ibfk_1
  foreign key (maj_cd) references mj (maj_cd),
  constraint course_ibfk_2
  foreign key (cor_cd) references gn (cor_cd)
)
  engine = InnoDB;

create index cor_cd
  on course (cor_cd);

create index maj_cd
  on course (maj_cd);


