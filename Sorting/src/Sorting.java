import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dao.Employee;


/**
 * @author dfrancisco
 *
 */

public class Sorting {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("employee.txt")));
			String line;
			List<Employee> employees = new ArrayList<Employee>();
			while ((line = br.readLine()) != null ) {
				//System.out.println(line);
				Employee employee = new Employee(line);
				employees.add(employee);
			}
			
			Collections.sort(employees, new Comparator<Employee>() {
				@Override
				public int compare(Employee o1, Employee o2) {
					// TODO Auto-generated method stub
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			for (Employee employee : employees) {
				
				System.out.format("%s %s %s\n", employee.getId(), employee.getName(), employee.getEmail());
				
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
