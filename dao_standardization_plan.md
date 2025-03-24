# DAO Standardization Plan

## Current Status

The project currently has duplicate DAO interfaces with inconsistent naming conventions:
- Mixed case (e.g., UserDao, ClassDao) vs. uppercase (e.g., UserDAO, ClassDAO)
- Different method signatures across implementations

## Standardization Approach

1. **Adopt consistent naming convention**
   - Standardize on mixed-case naming (e.g., UserDao, ClassDao)
   - This seems to be the more widely used convention in the project

2. **Interface consolidation**
   - Maintain both interfaces temporarily with clear documentation
   - Make uppercase versions (UserDAO) redirect to mixed-case versions (UserDao)
   - Eventually phase out uppercase versions

3. **Method naming standardization**
   - BaseDao: findById, findAll, save, update, delete
   - Entity-specific methods: find*, get* 

4. **Error handling**
   - Consistently throw SQLException
   - Use proper logging

## Implementation Plan

### Phase 1: Documentation and Redirection
- Add clear documentation to both interface versions
- Implement both interfaces in the Impl classes
- Make uppercase DAO methods delegate to mixed-case Dao methods

### Phase 2: Usage Migration
- Update controllers to use mixed-case Dao interfaces
- Test thoroughly to ensure functionality is preserved

### Phase 3: Removal of Duplicates
- Once all code is migrated to the mixed-case Dao interfaces
- Remove the uppercase DAO interfaces
- Retain documentation about the migration

## Affected Interfaces

1. **User**
   - UserDao (mixed-case) ✓
   - UserDAO (uppercase) ✓
   - UserDaoImpl (implementation) ✓
   - UserDAOImpl (implementation) ✓

2. **Department**
   - DepartmentDao (mixed-case) ✓
   - DepartmentDAO (uppercase) ✓
   - DepartmentDaoImpl (implementation) ✓
   - DepartmentDAOImpl (implementation) ✓

3. **Class**
   - ClassDao (mixed-case) ✓
   - ClassDAO (uppercase) ✓
   - ClassDaoImpl (implementation) ✓
   - ClassDAOImpl (implementation) ✓

4. **Subject**
   - SubjectDao (mixed-case) ✓
   - SubjectDAO (uppercase) ✓
   - SubjectDaoImpl (implementation) ✓
   - SubjectDAOImpl (implementation) ✓

5. **Attendance**
   - AttendanceDao (mixed-case) ✓
   - AttendanceDAO (uppercase) ✓
   - AttendanceDaoImpl (implementation) ✓
   - AttendanceDAOImpl (implementation) ✓

6. **Other**
   - LeaveApplicationDao
   - Other entities...

## Progress

- ✓ Updated UserDAO, UserDao with clear documentation
- ✓ Fixed implementations in UserDaoImpl, UserDAOImpl
- ✓ Updated BiometricAttendanceController to use standardized UserDao