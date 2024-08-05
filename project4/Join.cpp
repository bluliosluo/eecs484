#include "Join.hpp"

#include <vector>

#include <iostream>

using namespace std;

/*
 * Input: Disk, Memory, Disk page ids for left relation, Disk page ids for right relation
 * Output: Vector of Buckets of size (MEM_SIZE_IN_PAGE - 1) after partition
 */
/*
 * partition function
 *
 * Input:
 * disk: pointer of Disk object
 * mem: pointer of Memory object
 * left_rel: [left_rel.first, left_rel.second) will be the range of page ids of left relation to join
 * right_rel: [right_rel.first, right_rel.second) will be the range of page ids of right relation to join
 *
 * Output:
 * A vector of buckets of size (MEM_SIZE_IN_PAGE - 1).
 * Each bucket represent a partition of both relation.
 * See Bucket class for more information.
*/
vector<Bucket> partition(Disk* disk, Mem* mem, pair<uint, uint> left_rel,
                         pair<uint, uint> right_rel) {
	vector<Bucket> partitions(MEM_SIZE_IN_PAGE - 1, Bucket(disk));
	uint bucket_size = MEM_SIZE_IN_PAGE - 1;
	for (uint i = left_rel.first; i < left_rel.second; i++) {
		mem->loadFromDisk(disk, i, bucket_size);
		for (uint j = 0; j < mem->mem_page(bucket_size)->size(); j++) {
			Record r = mem->mem_page(bucket_size)->get_record(j);
			uint bucket_id = r.partition_hash() % (bucket_size);
			mem->mem_page(bucket_id)->loadRecord(r);
			//cout<<mem->mem_page(bucket_id)->size();
			if (mem->mem_page(bucket_id)->full()) {
				partitions[bucket_id].add_left_rel_page(mem->flushToDisk(disk, bucket_id));
			}
		}
		
	}
	for (uint i =0;i<bucket_size;i++){
		if (!mem->mem_page(i)->empty()){
			partitions[i].add_left_rel_page(mem->flushToDisk(disk, i));
		}
	}
	mem->reset(); 
	for (uint i = right_rel.first; i < right_rel.second; i++) {
		mem->loadFromDisk(disk, i, bucket_size);
		for (uint j = 0; j < mem->mem_page(bucket_size)->size(); j++) {
			Record r = mem->mem_page(bucket_size)->get_record(j);
			uint bucket_id = r.partition_hash() % (bucket_size);
			mem->mem_page(bucket_id)->loadRecord(r);
			if (mem->mem_page(bucket_id)->full()) {
				partitions[bucket_id].add_right_rel_page(mem->flushToDisk(disk, bucket_id));
			}
		}
	}
	for (uint i =0;i<bucket_size;i++){
		if (!mem->mem_page(i)->empty()){
			partitions[i].add_right_rel_page(mem->flushToDisk(disk, i));
		}
	}
	return partitions;
}

/*
 * Input: Disk, Memory, Vector of Buckets after partition
 * Output: Vector of disk page ids for join result
 */
/*
 * probe function
 * Input:
 * disk: pointer of Disk object
 * mem: pointer of Memory object
 * partition: a reference to a vector of buckets from partition function
 *
 * Output:
 * A vector of page ids that contains the join result.
*/
vector<uint> probe(Disk* disk, Mem* mem, vector<Bucket>& partitions) {
	vector<uint> disk_pages;
	mem->reset();
	uint bucket_size = MEM_SIZE_IN_PAGE - 1;
	for (uint i = 0; i < partitions.size(); i++) {
		if (partitions[i].get_left_rel().size() <= partitions[i].get_right_rel().size()) {
			for (uint j = 0; j < partitions[i].get_left_rel().size(); j++) {
				mem->loadFromDisk(disk, partitions[i].get_left_rel()[j], bucket_size);
				for (uint k = 0; k < mem->mem_page(bucket_size)->size(); k++) {
					Record r = mem->mem_page(bucket_size)->get_record(k);
					uint bucket_id = r.probe_hash() % (bucket_size-1);
					mem->mem_page(bucket_id)->loadRecord(r);
				}
			}

			for (uint l = 0; l < partitions[i].get_right_rel().size(); l++) {
				mem->loadFromDisk(disk, partitions[i].get_right_rel()[l], bucket_size);
				for (uint m = 0; m < mem->mem_page(bucket_size)->size(); m++) {
					Record s = mem->mem_page(bucket_size)->get_record(m);
					uint bucket_id = s.probe_hash() % (bucket_size-1);
					for (uint n = 0; n < mem->mem_page(bucket_id)->size(); n++) {
						Record t = mem->mem_page(bucket_id)->get_record(n);
						if (s==t) {
							mem->mem_page(bucket_size-1)->loadPair(s, t);
							if (mem->mem_page(bucket_size-1)->full()) {
								disk_pages.push_back(mem->flushToDisk(disk, bucket_size-1));
							}
						}
					}
				}
			}
		}
		else {
			for (uint j = 0; j < partitions[i].get_right_rel().size(); j++) {
				mem->loadFromDisk(disk, partitions[i].get_right_rel()[j], bucket_size);
				for (uint k = 0; k < mem->mem_page(bucket_size)->size(); k++) {
					Record r = mem->mem_page(bucket_size)->get_record(k);
					uint bucket_id = r.probe_hash() % (bucket_size-1);
					mem->mem_page(bucket_id)->loadRecord(r);
				}
			}

			for (uint l = 0; l < partitions[i].get_left_rel().size(); l++) {
				mem->loadFromDisk(disk, partitions[i].get_left_rel()[l], bucket_size);
				for (uint m = 0; m < mem->mem_page(bucket_size)->size(); m++) {
					Record s = mem->mem_page(bucket_size)->get_record(m);
					uint bucket_id = s.probe_hash() % (bucket_size-1);
					for (uint n = 0; n < mem->mem_page(bucket_id)->size(); n++) {
						Record t = mem->mem_page(bucket_id)->get_record(n);
						if (s==t) {
							mem->mem_page(bucket_size-1)->loadPair(s, t);
							if (mem->mem_page(bucket_size-1)->full()) {
								disk_pages.push_back(mem->flushToDisk(disk, bucket_size-1));
							}
						}
					}
				}
			}
		}
		for (uint i = 0; i < bucket_size-1; i++) {
			mem->mem_page(i)->reset();
		}
	}

	if (!mem->mem_page(bucket_size-1)->empty()) {
		disk_pages.push_back(mem->flushToDisk(disk, bucket_size-1));
	}
		
	
	return disk_pages;
}
