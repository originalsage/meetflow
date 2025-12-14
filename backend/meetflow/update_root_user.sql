-- 将root用户的role设置为2（超级管理员）
UPDATE user SET role = 2 WHERE username = 'root';

