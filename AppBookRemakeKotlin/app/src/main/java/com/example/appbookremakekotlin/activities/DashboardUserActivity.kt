package com.example.appbookremakekotlin.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.appbookremakekotlin.databinding.ActivityDashboardUserBinding
import com.example.appbookremakekotlin.fragments.BooksUserFragment
import com.example.appbookremakekotlin.model.CategoryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardUserBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<CategoryModel>
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this@DashboardUserActivity, ProfileActivity::class.java))
        }
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context):
        FragmentPagerAdapter(fm, behavior) {

        private val fragmentList: ArrayList<BooksUserFragment> = ArrayList()
        private val fragmentTitleList: ArrayList<String> = ArrayList()

        private val context:Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: BooksUserFragment, title:String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }

    }

    private fun setUpViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )

        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                val modelAll = CategoryModel("01", "All", 1, "")
                val modelMostViewed = CategoryModel("01", "Most Viewed", 1, "")
                val modelDownloaded = CategoryModel("01", "Most Downloaded", 1, "")

                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelDownloaded)

                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelDownloaded.id}",
                        "${modelDownloaded.category}",
                        "${modelDownloaded.uid}"
                    ), modelDownloaded.category
                )

                viewPagerAdapter.notifyDataSetChanged()

                for (ds in snapshot.children) {
                    val model = ds.getValue(CategoryModel::class.java)

                    categoryArrayList.add(model!!)

                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )

                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        viewPager.adapter = viewPagerAdapter
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null) {
            binding.subTitleTv.text = "Not Logged In"

            binding.btnProfile.visibility = View.GONE
        } else {
            val email = firebaseUser.email
            binding.subTitleTv.text = email

            binding.btnProfile.visibility = View.VISIBLE
        }
    }
}